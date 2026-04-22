package com.nex.nexmart.service.impl.order;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.constant.*;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.OrderMapper;
import com.nex.nexmart.model.dto.order.OrderCreateDTO;
import com.nex.nexmart.model.dto.order.OrderPreviewDTO;
import com.nex.nexmart.model.entity.*;
import com.nex.nexmart.model.entity.coupon.Coupon;
import com.nex.nexmart.model.entity.coupon.CouponUser;
import com.nex.nexmart.model.entity.order.Order;
import com.nex.nexmart.model.entity.order.OrderItem;
import com.nex.nexmart.model.entity.order.ReturnOrder;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.entity.product.ProductSpec;
import com.nex.nexmart.model.entity.review.Review;
import com.nex.nexmart.model.entity.seckill.SeckillItem;
import com.nex.nexmart.model.vo.order.OrderItemVO;
import com.nex.nexmart.model.vo.order.OrderPreviewVO;
import com.nex.nexmart.model.vo.order.OrderVO;
import com.nex.nexmart.model.vo.order.ReturnOrderVO;
import com.nex.nexmart.service.intf.coupon.CouponService;
import com.nex.nexmart.service.intf.coupon.CouponUserService;
import com.nex.nexmart.service.intf.*;
import com.nex.nexmart.service.intf.home.HomeSectionItemService;
import com.nex.nexmart.service.intf.home.HomeSectionService;
import com.nex.nexmart.service.intf.order.OrderItemService;
import com.nex.nexmart.service.intf.order.OrderService;
import com.nex.nexmart.service.intf.order.ReturnOrderService;
import com.nex.nexmart.service.intf.product.ProductService;
import com.nex.nexmart.service.intf.product.ProductSpecService;
import com.nex.nexmart.service.intf.review.ReviewService;
import com.nex.nexmart.service.intf.seckill.SeckillItemService;
import com.nex.nexmart.websocket.WebSocketSessionManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

	private final OrderItemService orderItemService;
	private final CartItemService cartItemService;
	private final ProductService productService;
	private final ReviewService reviewService;
	private final ProductSpecService productSpecService;
	private final ReturnOrderService returnOrderService;
	private final WebSocketSessionManager sessionManager;
	private final PromotionService promotionService;
	private final CouponUserService couponUserService;
	private final CouponService couponService;
	private final SeckillItemService seckillItemService;
	private final RabbitTemplate rabbitTemplate;
	private final RedisTemplate<String, String> redisTemplate;
	private static final String HOME_MANUAL_SECTION_KEY_PREFIX = "NexMart:home:section:manual:";
	private static final String HOME_AUTO_SECTION_KEY_PREFIX = "NexMart:home:section:auto:";

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String createOrder(@Valid OrderCreateDTO dto, Long userId) {
		List<CartItem> cartItems = cartItemService.listByIds(dto.getCartItemIds());
		if (cartItems.isEmpty()) {
			throw new BusinessException("购物车条目不存在");
		}
		boolean ownerError = cartItems.stream().anyMatch(c -> !c.getUserId().equals(userId));
		if (ownerError) {
			throw new BusinessException("非法操作");
		}

		List<Long> productIds =
				cartItems.stream()
						.map(CartItem::getProductId)
						.collect(Collectors.toList());
		List<Product> products = productService.lambdaQuery()
				.in(Product::getId, productIds)
				.list();
		Map<Long, Product> productMap = products
				.stream()
				.collect(Collectors.toMap(Product::getId, p -> p));

		//为优惠活动匹配做准备
		List<Long> categoryIds = products.stream().map(Product::getCategoryId).distinct().toList();

		// specId 过滤掉 null 再查
		List<Long> specIds = cartItems.stream()
				.map(CartItem::getSpecId)
				.filter(Objects::nonNull) //.filter(specId -> specId != null)
				.collect(Collectors.toList());

		Map<Long, ProductSpec> specMap = specIds.isEmpty()
				? Collections.emptyMap()
				: productSpecService.lambdaQuery()
				.in(ProductSpec::getId, specIds)
				.list()
				.stream()
				.collect(Collectors.toMap(ProductSpec::getId, s -> s));

		// 一次性查出所有当前生效的活动
		List<Promotion> activePromotions = promotionService.getActivePromotionList(productIds, categoryIds);

		List<OrderItem> orderItems = new ArrayList<>();

		Map<Long, Long> productCouponMap = dto.getProductCouponMap();

		List<CouponUser> couponUsers = new ArrayList<>();
		if (productCouponMap != null && !productCouponMap.isEmpty()) {
			List<Long> userCouponIds = new ArrayList<>(productCouponMap.values());
			couponUsers = couponUserService.lambdaQuery()
					.in(CouponUser::getId, userCouponIds)
					.list();
		}

		Map<Long, CouponUser> couponUserMap = couponUsers.stream()
				.collect(Collectors.toMap(CouponUser::getId, c -> c));
		List<Long> couponIds = couponUsers
					.stream()
					.map(CouponUser::getCouponId)
					.distinct()
					.toList();
		Map<Long, String> couponNameMap = new HashMap<>();
		if (!couponIds.isEmpty()) {
			couponNameMap = couponService.lambdaQuery()
					.in(Coupon::getId, couponIds)
					.list()
					.stream().collect(Collectors.toMap(Coupon::getId, Coupon::getName));
		}


		//获取价格相关数据
		OrderPreviewDTO previewDTO = new OrderPreviewDTO();
		previewDTO.setCartItemIds(dto.getCartItemIds());
		previewDTO.setOrderUserCouponId(dto.getOrderUserCouponId());
		previewDTO.setProductCouponMap(productCouponMap);
		OrderPreviewVO previewVO = preview(userId, previewDTO);
		Map<Long, BigDecimal> productCouponDiscountMap = previewVO.getProductCouponDiscountMap();

		for (CartItem cartItem : cartItems) {
			Product product = productMap.get(cartItem.getProductId());
			if (product == null || product.getStatus() == 0) {
				throw new BusinessException("该商品已下架");
			}

			OrderItem item = new OrderItem();
			item.setProductId(product.getId());
			item.setProductName(product.getName());
			item.setCoverUrl(product.getCoverUrl());
			item.setPrice(product.getPrice());
			item.setQuantity(cartItem.getQuantity());

			if (cartItem.getSpecId() != null) {
				// 有规格商品：从 specMap 中取出对应规格
				ProductSpec spec = specMap.get(cartItem.getSpecId());

				// 校验规格库存是否充足
				if (spec.getStock() < cartItem.getQuantity()) {
					throw new BusinessException("商品 [" + product.getName() + "] 库存不足");
				}

				// 扣减规格库存
				int newSpecStock = spec.getStock() - cartItem.getQuantity();
				productSpecService.lambdaUpdate()
						.eq(ProductSpec::getId, spec.getId())
						.set(ProductSpec::getStock, newSpecStock)
						.update();

				// 同步扣减商品总库存，总库存为 0 时自动设置售空
				int newProductStock = product.getStock() - cartItem.getQuantity();
				// 商品售空时，清除首页商品缓存以及DB
				if (newProductStock == 0) {
					clearHomeSectionAllCache();
				}
				productService.lambdaUpdate()
						.eq(Product::getId, product.getId())
						.set(Product::getStock, newProductStock)
						.set(newProductStock == 0, Product::getStatus, 2)
						.update();

				// 将规格名称写入订单项快照，防止规格被修改或删除后历史订单无法展示
				item.setSpecName(spec.getSpecName());
			} else {
				// 无规格商品：直接校验商品总库存
				if (product.getStock() < cartItem.getQuantity()) {
					throw new BusinessException("商品 [" + product.getName() + "] 库存不足");
				}

				// 扣减商品库存，库存为 0 时自动下架
				int newStock = product.getStock() - cartItem.getQuantity();
				if (newStock == 0) {
					clearHomeSectionAllCache();
				}
				productService.lambdaUpdate()
						.eq(Product::getId, product.getId())
						.set(Product::getStock, newStock)
						.set(newStock == 0, Product::getStatus, 2)
						.update();
			}

			Promotion best = promotionService.findBestPromotion(product, activePromotions);
			if (best != null) {
				item.setPromotionName(best.getName());
				item.setPromotionalPrice(promotionService.calcDiscountedPrice(product.getPrice(), best));
			}
			if (productCouponMap != null && productCouponMap.get(cartItem.getId()) != null) {
				Long userCouponId = productCouponMap.get(cartItem.getId());
				CouponUser couponUser = couponUserMap.get(userCouponId); // 之前批量查好的
				if (couponUser != null) {
					String couponName = couponNameMap.get(couponUser.getCouponId());
					item.setCouponName(couponName);
					item.setCouponDiscount(productCouponDiscountMap.get(cartItem.getId()));
				}
			}
			orderItems.add(item);
		}

		String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
				+ String.format("%06d", new Random().nextInt(999999));

		Order order = new Order();
		order.setUserId(userId);
		order.setOrderNo(orderNo);
		order.setStatus(1);
		order.setPayStatus(0);
		BeanUtils.copyProperties(dto, order);
		BeanUtils.copyProperties(previewVO, order);
		//保存订单券名称
		if (dto.getOrderUserCouponId() != null){
			CouponUser cu = couponUserService.getById(dto.getOrderUserCouponId());
			Coupon coupon = couponService.lambdaQuery().eq(Coupon::getId, cu.getCouponId()).one();
			order.setOrderCouponName(coupon.getName());
		}
		save(order);

		// 核销订单券
		if (dto.getOrderUserCouponId() != null) {
			CouponUser cu = couponUserService.getById(dto.getOrderUserCouponId());
			cu.setStatus(1);
			cu.setUsedAt(LocalDateTime.now());
			cu.setOrderId(order.getId());//要先保存订单,才有id
			couponUserService.updateById(cu);
		}

		// 核销商品券
		if (dto.getProductCouponMap() != null) {
			for (Long userCouponId : dto.getProductCouponMap().values()) {
				CouponUser cu = couponUserService.getById(userCouponId);
				cu.setStatus(1);
				cu.setUsedAt(LocalDateTime.now());
				cu.setOrderId(order.getId());
				couponUserService.updateById(cu);
			}
		}

		orderItems.forEach(item -> item.setOrderId(order.getId()));
		orderItemService.saveBatch(orderItems);
		cartItemService.removeByIds(dto.getCartItemIds());

		// 发送15分钟超时取消消息
		Map<String, Object> msg = new HashMap<>();
		msg.put("orderId", order.getId());
		rabbitTemplate.convertAndSend(
				RabbitMQConstants.ORDER_DELAY_EXCHANGE,
				RabbitMQConstants.ORDER_DELAY_ROUTING_KEY,
				msg
		);
		return orderNo;
	}


	@Override
	//已优化数据库查询次数
	public PageResult<OrderVO> OrdersPage(long current, long size, Long userId, Integer status, String keyword) {
		// 1. 查分页订单（1次）
		Page<Order> page = lambdaQuery()
				.eq(userId!=null,Order::getUserId, userId)
				.eq(status != null, Order::getStatus, status)
				.eq(keyword != null, Order::getOrderNo, keyword)
				.orderByDesc(Order::getCreatedAt)
				.page(new Page<>(current, size));
		List<Order> orders = page.getRecords();

		// 2. 批量查所有订单的订单项（1次）
		List<Long> orderIds = orders.stream().map(Order::getId).toList();
		List<OrderItem> allItems;
		if (orderIds.isEmpty()) {
			allItems = Collections.emptyList();
		} else {
			allItems = orderItemService.lambdaQuery()
					.in(OrderItem::getOrderId, orderIds).list();
		}

		// 3. 按 orderId 分组
		Map<Long, List<OrderItem>> itemsGroupByOrderId = allItems.stream()
				.collect(Collectors.groupingBy(OrderItem::getOrderId));

		// 4. 批量查所有订单项的 Review（1次）
		List<Long> allItemIds = allItems.stream().map(OrderItem::getId).toList();
		Map<Long, Review> reviewMap = allItemIds.isEmpty() ? Map.of()
				: reviewService.lambdaQuery().in(Review::getOrderItemId, allItemIds).list()
				.stream().collect(Collectors.toMap(Review::getOrderItemId, r -> r));

		// 5. 批量查所有订单项的 ReturnOrder（1次）
		Map<Long, ReturnOrder> returnOrderMap = allItemIds.isEmpty() ? Map.of()
				: returnOrderService.lambdaQuery().in(ReturnOrder::getOrderItemId, allItemIds).list()
				.stream().collect(Collectors.toMap(ReturnOrder::getOrderItemId, r -> r));

		// 6. 组装 VO
		List<OrderVO> orderVoList = convertToVOList(orders, itemsGroupByOrderId, reviewMap, returnOrderMap);

		// 7. 返回PageResult
		 return PageResult.of(orderVoList, page.getTotal(), page.getCurrent(), page.getSize());

	}

	@Override
	public OrderVO orderDetail(Long id, Long userId) {
		Order order = getById(id);
		if (order == null || !order.getUserId().equals(userId)) {
			throw new BusinessException("订单不存在");
		}
		return convertToVO(order);
	}

	@Override
	public void cancelOrder(Long id, Long userId) {
		Order order = getById(id);
		if (order == null || !order.getUserId().equals(userId)) {
			throw new BusinessException("订单不存在");
		}
		if (order.getStatus() != OrderStatusConstants.PENDING_PAYMENT) {
			throw new BusinessException("只有待付款的订单可以取消");
		}
		cancelOrderRollback(order);
		// 发送消息给管理员
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			String message = JSON.toJSONString(Map.of(
					"type", "CANCEL_ORDER",
					"orderId", order.getId(),
					"amount", order.getFinalAmount(),
					"time", LocalDateTime.now().format(formatter)
			));
			sessionManager.broadcast(message);
	}

	public void cancelOrderByTimeout(Long orderId) {
		Order order = getById(orderId);
		// 只处理待付款的订单
		if (order == null || order.getStatus() != OrderStatusConstants.PENDING_PAYMENT) {
			return;
		}
		//回滚
		cancelOrderRollback(order);
		// 回滚Redis购买记录
		if (order.getSeckillItemId() != null) {
			String stockKey = RedisSeckillConstants.SECKILL_PRODUCT_STOCK + order.getSeckillItemId();
			String userKey = RedisSeckillConstants.SECKILL_PRODUCT_USERS + order.getSeckillItemId();
			redisTemplate.opsForValue().increment(stockKey);
			redisTemplate.opsForHash().increment(userKey, String.valueOf(order.getUserId()), -1);
		}
	}

	private void cancelOrderRollback(Order order){
		// 更新订单状态
		lambdaUpdate()
				.eq(Order::getId, order.getId())
				.set(Order::getStatus, OrderStatusConstants.CANCELLED)
				.update();

		List<OrderItem> items = orderItemService.lambdaQuery()
				.eq(OrderItem::getOrderId, order.getId())
				.list();
		// 回滚库存
		for (OrderItem item : items) {
			if (order.getSeckillItemId() != null) {
				// 秒杀订单只还 seckill_stock
				seckillItemService.lambdaUpdate()
						.eq(SeckillItem::getId, order.getSeckillItemId())
						.setSql("seckill_stock = seckill_stock + 1")
						.update();
			} else {
				// 普通订单还 product.stock 和 spec.stock
				productService.lambdaUpdate()
						.eq(Product::getId, item.getProductId())
						.setSql("stock = stock + 1")
						.update();
				if (item.getSpecName() != null) {
					productSpecService.lambdaUpdate()
							.eq(ProductSpec::getProductId, item.getProductId())
							.eq(ProductSpec::getSpecName, item.getSpecName())
							.setSql("stock = stock + 1")
							.update();
				}
			}
		}
		// 回滚商品券与订单券
		couponUserService.lambdaUpdate()
				.eq(CouponUser::getOrderId, order.getId())
				.eq(CouponUser::getStatus, 1)
				.set(CouponUser::getStatus, 0)
				.set(CouponUser::getUsedAt, null)
				.set(CouponUser::getOrderId, null)
				.update();
	}

	@Override
	public void confirmReceipt(Long id, Long userId) {
		Order order = getById(id);
		if (order == null || !order.getUserId().equals(userId)) {
			throw new BusinessException("订单不存在");
		}

		if (order.getStatus() != 3) {
			throw new BusinessException("只有待收货的订单可以确认收货");
		}

		lambdaUpdate()
				.eq(Order::getId, id)
				.set(Order::getStatus, 4)
				.set(Order::getCompleteTime, LocalDateTime.now())
				.update();

		//更新商品销量
		List<OrderItem> orderItems = orderItemService.lambdaQuery()
				.eq(OrderItem::getOrderId, id)
				.list();
		// 按商品单独更新销量，因为每个商品购买数量不同
		for (OrderItem item : orderItems) {
			productService.lambdaUpdate()
					.eq(Product::getId, item.getProductId())
					.setSql("sales = sales + " + item.getQuantity())
					.update();
		}

		// 更新秒杀商品已售数量
		if(order.getSeckillItemId()!=null){
			seckillItemService.lambdaUpdate()
					.eq(SeckillItem::getId, order.getSeckillItemId())
					.setSql("sold_count = sold_count + 1")
					.update();
		}

	}


	@Override
	public void rebuy(Long id, Long userId) {
		// 1. 校验订单是否属于该用户
		Order order = getById(id);
		if (order == null || !order.getUserId().equals(userId)) {
			throw new BusinessException("订单不存在");
		}
		// 2. 校验订单状态
		if (order.getStatus() != OrderStatusConstants.COMPLETED&&order.getStatus() != OrderStatusConstants.CANCELLED) {
			throw new BusinessException("只有已完成或已取消的订单可以再次购买");
		}

		// 3. 查询订单项
		List<OrderItem> orderItems = orderItemService.lambdaQuery()
				.eq(OrderItem::getOrderId, id)
				.list();
		if (orderItems.isEmpty()) {
			throw new BusinessException("订单项不存在");
		}

		// 4. 逐个校验商品状态并加入购物车
		for (OrderItem item : orderItems) {
			Product product = productService.getById(item.getProductId());
			if (product == null || !product.getStatus() .equals(ProductStatusConstant.LISTED) ) {
				throw new BusinessException("商品【" + item.getProductName() + "】已下架");
			}
			if (product.getStock() < item.getQuantity()) {
				throw new BusinessException("商品【" + item.getProductName() + "】库存不足");
			}

			// 5. 加入购物车（已存在则数量叠加）
			CartItem cartItem = cartItemService.lambdaQuery()
					.eq(CartItem::getUserId, userId)
					.eq(CartItem::getProductId, item.getProductId())
					.one();
			if (cartItem != null) {
				// 已在购物车中，数量叠加
				cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
				cartItemService.updateById(cartItem);
			} else {
				// 不在购物车，新增
				CartItem newCartItem = new CartItem();
				newCartItem.setUserId(userId);
				newCartItem.setProductId(item.getProductId());
				newCartItem.setQuantity(item.getQuantity());
				cartItemService.save(newCartItem);
			}
		}
	}

	@Override
	public void updateStatus(Long id, Integer status) {
		Order order = getById(id);
		if (order == null) {
			throw new BusinessException("订单不存在");
		}
		lambdaUpdate()
				.eq(Order::getId, id)
				.set(Order::getStatus, status)
				.update();
	}

	@Override
	public OrderPreviewVO preview(Long userId, OrderPreviewDTO dto) {
		// 查购物车项和商品
		List<CartItem> cartItems = cartItemService.listByIds(dto.getCartItemIds());
		List<Long> productIds = cartItems.stream()
				.map(CartItem::getProductId)
				.toList();
		List<Product> products = productService.listByIds(productIds);
		Map<Long, Product> productMap = products.stream()
				.collect(Collectors.toMap(Product::getId, p -> p));
		List<Long> categoryIds = products.stream().map(Product::getCategoryId).distinct().toList();


		// 按 cartItemId 建索引，方便后面商品券匹配
		Map<Long, CartItem> cartItemMap = cartItems.stream()
				.collect(Collectors.toMap(CartItem::getId, c -> c));

		// 计算原始总价和 promotion 优惠
		BigDecimal originalAmount = BigDecimal.ZERO;
		BigDecimal promotionDiscount = BigDecimal.ZERO;

		List<Promotion> promotionList = promotionService.getActivePromotionList(productIds, categoryIds);
		for (CartItem item : cartItems) {
			Product product = productMap.get(item.getProductId());
			if (product == null) continue;
			originalAmount = originalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

			Promotion best = promotionService.findBestPromotion(product, promotionList);
			if (best != null) {
				BigDecimal discountedPrice = promotionService.calcDiscountedPrice(product.getPrice(),best);
				if (discountedPrice != null) {
					promotionDiscount = promotionDiscount.add(
							product.getPrice().subtract(discountedPrice).multiply(BigDecimal.valueOf(item.getQuantity())));
				}
			}
		}

		BigDecimal afterPromotion = originalAmount.subtract(promotionDiscount);

		Map<Long, BigDecimal> productCouponDiscountMap = new HashMap<>();

		OrderPreviewVO vo = new OrderPreviewVO();
		vo.setOriginalAmount(originalAmount);
		vo.setPromotionTotalDiscount(promotionDiscount);
		vo.setOrderCouponDiscount(null);
		vo.setProductCouponTotalDiscount(null);
		vo.setFinalAmount(afterPromotion);
		vo.setOrderCouponUsable(null);

		// 计算商品券优惠
		BigDecimal productCouponDiscount = BigDecimal.ZERO;
		if (dto.getProductCouponMap() != null && !dto.getProductCouponMap().isEmpty()) {
			for (Map.Entry<Long, Long> entry : dto.getProductCouponMap().entrySet()) {
				Long cartItemId = entry.getKey();
				Long userCouponId = entry.getValue();

				CartItem item = cartItemMap.get(cartItemId);
				if (item == null) continue;

				// 校验券
				CouponUser couponUser = couponUserService.getById(userCouponId);
				if (couponUser == null || !couponUser.getUserId().equals(userId)
						|| couponUser.getStatus() != 0
						|| couponUser.getExpireAt().isBefore(LocalDateTime.now())) continue;

				Coupon coupon = couponService.getById(couponUser.getCouponId());
				if (coupon == null || coupon.getCouponType() != 1) continue;

				// 校验券适用范围
				Product product = productMap.get(item.getProductId());
				if (product == null) continue;
				if (!isProductCouponApplicable(coupon, product)) continue;

				// 计算这张商品券的优惠（基于商品单价）
				BigDecimal discount = calcProductCouponDiscount(coupon, product.getPrice());
				if (discount != null) {
					productCouponDiscount = productCouponDiscount.add(discount);
					productCouponDiscountMap.put(item.getId(), discount);
				}
			}
		}

		vo.setProductCouponTotalDiscount(productCouponDiscount);
		vo.setProductCouponDiscountMap(productCouponDiscountMap);
		BigDecimal afterProductCoupon = afterPromotion.subtract(productCouponDiscount);

		// 计算订单券优惠
		if (dto.getOrderUserCouponId() == null) {
			vo.setFinalAmount(afterProductCoupon.max(BigDecimal.ZERO));
			return vo;
		}

		CouponUser orderCouponUser = couponUserService.getById(dto.getOrderUserCouponId());
		if (orderCouponUser == null || !orderCouponUser.getUserId().equals(userId)
				|| orderCouponUser.getStatus() != 0
				|| orderCouponUser.getExpireAt().isBefore(LocalDateTime.now())) {
			vo.setOrderCouponUsable(false);
			vo.setFinalAmount(afterProductCoupon.max(BigDecimal.ZERO));
			vo.setOrderCouponUsableReason("此订单券不存在或已使用或已过期");
			return vo;
		}

		Coupon orderCoupon = couponService.getById(orderCouponUser.getCouponId());
		if (orderCoupon == null || orderCoupon.getCouponType() != 2) {
			vo.setOrderCouponUsable(false);
			vo.setFinalAmount(afterProductCoupon.max(BigDecimal.ZERO));
			vo.setOrderCouponUsableReason("此订单券不存在，请联系工作人员");
			return vo;
		}

		// 秒杀订单券不能与 promotion 叠加
		if (promotionDiscount.compareTo(BigDecimal.ZERO) > 0) {
			vo.setOrderCouponUsable(false);
			vo.setFinalAmount(afterProductCoupon.max(BigDecimal.ZERO));
			vo.setOrderCouponUsableReason("此订单券不能与该订单的促销活动叠加使用");
			return vo;
		}

		// 计算订单券优惠（减商品券后的金额）
		BigDecimal orderCouponDiscount = calcOrderCouponDiscount(orderCoupon, afterProductCoupon);
		if (orderCouponDiscount == null) {
			vo.setOrderCouponUsable(false);
			vo.setFinalAmount(afterProductCoupon.max(BigDecimal.ZERO));
			return vo;
		}

		vo.setOrderCouponDiscount(orderCouponDiscount);
		vo.setOrderCouponUsable(true);
		vo.setFinalAmount(afterProductCoupon.subtract(orderCouponDiscount).max(BigDecimal.ZERO));
		return vo;
	}

	// 判断商品券是否适用于该商品
	private boolean isProductCouponApplicable(Coupon coupon, Product product) {
		return switch (coupon.getScope()) {
			case 1 -> true; // 全场
			case 2 -> product.getCategoryId().equals(coupon.getScopeId()); // 单分类
			case 3 -> product.getId().equals(coupon.getScopeId()); // 单商品
			default -> false;
		};
	}

	// 计算商品券优惠（基于商品单价）
	private BigDecimal calcProductCouponDiscount(Coupon coupon, BigDecimal price) {
		return switch (coupon.getDiscountType()) {
			case 1 -> price.compareTo(coupon.getMinAmount()) >= 0
					? coupon.getDiscountAmount() : null; // 满减
			case 2 -> price.subtract(
					price.multiply(coupon.getDiscountRate())
							.setScale(2, RoundingMode.HALF_UP)); // 折扣
			case 3 -> coupon.getDiscountAmount(); // 无门槛
			default -> null;
		};
	}

	// 计算订单券优惠
	private BigDecimal calcOrderCouponDiscount(Coupon coupon, BigDecimal baseAmount) {
		return switch (coupon.getDiscountType()) {
			case 2 -> baseAmount.subtract(
					baseAmount.multiply(coupon.getDiscountRate())
							.setScale(2, RoundingMode.HALF_UP)); // 折扣
			case 3 -> coupon.getDiscountAmount(); // 无门槛
			default -> null;
		};
	}

	private OrderVO convertToVO(Order order) {
		OrderVO vo = new OrderVO();
		BeanUtils.copyProperties(order, vo);
		vo.setStatusDesc(getStatusDesc(order.getStatus()));

		List<OrderItem> items = orderItemService.lambdaQuery()
				.eq(OrderItem::getOrderId, order.getId())
				.list();

		List<Long> itemIds = items.stream().map(OrderItem::getId).collect(Collectors.toList());

		Map<Long, Review> reviewMap = itemIds.isEmpty()
				? Map.of()
				: reviewService.lambdaQuery().in(Review::getOrderItemId, itemIds).list()
				.stream().collect(Collectors.toMap(Review::getOrderItemId, r -> r));

		List<OrderItemVO> itemVOs = items.stream().map(item -> {
			OrderItemVO itemVO = new OrderItemVO();
			BeanUtils.copyProperties(item, itemVO);
			BigDecimal originalAmount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
			BigDecimal promotionDiscount = item.getPromotionalPrice() != null
					? originalAmount.subtract(item.getPromotionalPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
					: BigDecimal.ZERO;
			BigDecimal coupon = Optional.ofNullable(item.getCouponDiscount()).orElse(BigDecimal.ZERO);
			itemVO.setOriginalAmount(originalAmount);
			itemVO.setPromotionDiscount(promotionDiscount);
			itemVO.setFinalAmount(originalAmount.subtract(promotionDiscount).subtract(coupon));
			if(item.getSeckillPrice()!=null){
				itemVO.setFinalAmount(item.getSeckillPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
			}
			Review review = reviewMap.get(item.getId());
			if (review != null) {
				itemVO.setReviewed(true);
				itemVO.setReviewId(review.getId());
			} else {
				itemVO.setReviewed(false);
			}
			// 查询该订单项的退货信息
			ReturnOrder returnOrder = returnOrderService.lambdaQuery()
					.eq(ReturnOrder::getOrderItemId, item.getId())
					.one();
			if (returnOrder != null) {
				ReturnOrderVO returnOrderVO = new ReturnOrderVO();
				BeanUtils.copyProperties(returnOrder, returnOrderVO);
				returnOrderVO.setImages(returnOrder.getImages() != null
						? Arrays.asList(returnOrder.getImages().split(","))
						: null);
				// 用枚举或常量转换状态描述
				returnOrderVO.setStatusDesc(getReturnStatusDesc(returnOrder.getStatus()));
				itemVO.setReturnOrder(returnOrderVO);
			}
			return itemVO;
		}).collect(Collectors.toList());
		vo.setItems(itemVOs);
		vo.setTotalQuantity(items.stream().mapToInt(OrderItem::getQuantity).sum());
		return vo;
	}

	private List<OrderVO> convertToVOList(List<Order>  orders,
	                            Map<Long, List<OrderItem>> itemsGroupByOrderId,
	                            Map<Long, Review> reviewMap,
	                            Map<Long, ReturnOrder> returnOrderMap) {
		return orders.stream().map(order -> {
			OrderVO vo = new OrderVO();
			BeanUtils.copyProperties(order, vo);
			vo.setStatusDesc(getStatusDesc(order.getStatus()));
			List<OrderItem> items = itemsGroupByOrderId.getOrDefault(order.getId(), List.of());
			List<OrderItemVO> orderItemVOs = items.stream().map(item -> {
				OrderItemVO itemVO = new OrderItemVO();
				BeanUtils.copyProperties(item, itemVO);
				BigDecimal originalAmount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
				BigDecimal promotionDiscount = item.getPromotionalPrice() != null
						? originalAmount.subtract(item.getPromotionalPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
						: BigDecimal.ZERO;
				BigDecimal coupon = Optional.ofNullable(item.getCouponDiscount()).orElse(BigDecimal.ZERO);
				itemVO.setOriginalAmount(originalAmount);
				itemVO.setPromotionDiscount(promotionDiscount);
				itemVO.setFinalAmount(originalAmount.subtract(promotionDiscount).subtract(coupon));
				if(item.getSeckillPrice()!=null){
					itemVO.setFinalAmount(item.getSeckillPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
				}
				//是否评价
				Review review = reviewMap.get(item.getId());
				itemVO.setReviewed(review != null);
				if (review != null) {
					itemVO.setReviewId(review.getId());
				}
				//是否退货
				ReturnOrder returnOrder = returnOrderMap.get(item.getId());
				if (returnOrder != null) {
					ReturnOrderVO returnOrderVO = new ReturnOrderVO();
					BeanUtils.copyProperties(returnOrder, returnOrderVO);
					returnOrderVO.setImages(returnOrder.getImages() != null
							? Arrays.asList(returnOrder.getImages().split(","))
							: null);
					// 用枚举或常量转换状态描述
					returnOrderVO.setStatusDesc(getReturnStatusDesc(returnOrder.getStatus()));
					itemVO.setReturnOrder(returnOrderVO);
				}
				return itemVO;
			}).toList();
			vo.setItems(orderItemVOs);
			vo.setTotalQuantity(items.stream().mapToInt(OrderItem::getQuantity).sum());
			return vo;
		}).toList();
	}

	private String getReturnStatusDesc(Integer status) {
		return switch (status) {
			case ReturnOrderStatusConstant.APPLYING          -> "退货申请中";
			case ReturnOrderStatusConstant.APPROVED          -> "退货已批准，请寄回商品";
			case ReturnOrderStatusConstant.REJECTED          -> "退货已拒绝";
			case ReturnOrderStatusConstant.REFUND_PROCESSING -> "退款处理中";
			case ReturnOrderStatusConstant.REFUNDED          -> "退款已完成";
			case ReturnOrderStatusConstant.CANCELED          -> "申请退款已取消";
			default                                          -> "未知状态";
		};
	}

	public String getStatusDesc(Integer status) {
		return switch (status) {
			case 0 -> "已取消";
			case 1 -> "待付款";
			case 2 -> "待发货";
			case 3 -> "待收货";
			case 4 -> "已完成";
			default -> "未知";
		};
	}

	public void clearHomeSectionAllCache(){
		List<String> keys = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			keys.add(HOME_MANUAL_SECTION_KEY_PREFIX + i);
			keys.add(HOME_AUTO_SECTION_KEY_PREFIX + i);
		}
		redisTemplate.delete(keys);
	}
}
