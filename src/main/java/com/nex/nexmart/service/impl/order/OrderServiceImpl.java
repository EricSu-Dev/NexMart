package com.nex.nexmart.service.impl.order;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.constant.*;
import com.nex.nexmart.config.RabbitMQConfig;
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
import com.nex.nexmart.service.intf.order.OrderItemService;
import com.nex.nexmart.service.intf.order.OrderService;
import com.nex.nexmart.service.intf.order.ReturnOrderService;
import com.nex.nexmart.service.intf.product.ProductService;
import com.nex.nexmart.service.intf.product.ProductSpecService;
import com.nex.nexmart.service.intf.review.ReviewService;
import com.nex.nexmart.service.intf.seckill.SeckillItemService;
import com.nex.nexmart.websocket.WebSocketSessionManager;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

	@Resource(name = "nexmartExecutor")
	private ThreadPoolExecutor executor;

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
	private final RabbitMQConfig rabbitMQConfig;
	private final RedisTemplate<String, String> redisTemplate;
	private static final String HOME_MANUAL_SECTION_KEY_PREFIX = "NexMart:home:section:manual:";
	private static final String HOME_AUTO_SECTION_KEY_PREFIX = "NexMart:home:section:auto:";

	private static final DefaultRedisScript<Long> SECKILL_ROLLBACK_LUA_SCRIPT =
			new DefaultRedisScript<>(RedisSeckillConstants.SECKILL_ROLLBACK_LUA, Long.class);

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String createOrder(@Valid OrderCreateDTO dto, Long userId) {
		// 校验购物车
		List<CartItem> cartItems = cartItemService.listByIds(dto.getCartItemIds());
		if (cartItems.isEmpty()) throw new BusinessException("购物车条目不存在");
		if (cartItems.stream().anyMatch(c -> !c.getUserId().equals(userId))) {
			throw new BusinessException("非法操作");
		}

		// 查商品及分类
		List<Long> productIds = cartItems.stream().map(CartItem::getProductId).toList();
		List<Product> products = productService.lambdaQuery().in(Product::getId, productIds).list();
		Map<Long, Product> productMap = products.stream()
				.collect(Collectors.toMap(Product::getId, p -> p));
		List<Long> categoryIds = products.stream().map(Product::getCategoryId).distinct().toList();

		// 并行查规格以及查促销活动
		List<Long> specIds = cartItems.stream()
				.map(CartItem::getSpecId).filter(Objects::nonNull).toList();
		CompletableFuture<Map<Long, ProductSpec>> specFuture = specIds.isEmpty()
				? CompletableFuture.completedFuture(Collections.emptyMap())
				: CompletableFuture.supplyAsync(() ->
						productSpecService.lambdaQuery().in(ProductSpec::getId, specIds).list()
								.stream().collect(Collectors.toMap(ProductSpec::getId, s -> s))
				, executor);
		CompletableFuture<List<Promotion>> promotionFuture = CompletableFuture.supplyAsync(() ->
				promotionService.getActivePromotionList(productIds, categoryIds), executor);

		CompletableFuture.allOf(specFuture, promotionFuture).join();
		Map<Long, ProductSpec> specMap = specFuture.join();
		List<Promotion> activePromotions = promotionFuture.join();

		// 批量查商品券并存入Map
		Map<Long, Long> productCouponMap = dto.getProductCouponMap();
		Map<Long, CouponUser> couponUserMap = Collections.emptyMap();
		Map<Long, String> couponNameMap = Collections.emptyMap();
		if (productCouponMap != null && !productCouponMap.isEmpty()) {
			List<CouponUser> couponUsers = couponUserService.lambdaQuery()
					.in(CouponUser::getId, new ArrayList<>(productCouponMap.values())).list();
			couponUserMap = couponUsers.stream()
					.collect(Collectors.toMap(CouponUser::getId, c -> c));
			List<Long> couponIds = couponUsers.stream().map(CouponUser::getCouponId).distinct().toList();
			couponNameMap = couponService.lambdaQuery().in(Coupon::getId, couponIds).list()
					.stream().collect(Collectors.toMap(Coupon::getId, Coupon::getName));
		}

		// 查订单券
		CouponUser orderCouponUser = null;
		Coupon orderCoupon = null;
		if (dto.getOrderUserCouponId() != null) {
			orderCouponUser = couponUserService.getById(dto.getOrderUserCouponId());
			if (orderCouponUser != null) {
				orderCoupon = couponService.lambdaQuery()
						.eq(Coupon::getId, orderCouponUser.getCouponId()).one();
			}
		}

		// 调 preview 计算价格
		OrderPreviewDTO previewDTO = new OrderPreviewDTO();
		previewDTO.setCartItemIds(dto.getCartItemIds());
		previewDTO.setOrderUserCouponId(dto.getOrderUserCouponId());
		previewDTO.setProductCouponMap(productCouponMap);
		OrderPreviewVO previewVO = preview(userId, previewDTO);
		Map<Long, BigDecimal> productCouponDiscountMap = previewVO.getProductCouponDiscountMap();

		// 组装订单项
		List<OrderItem> orderItems = new ArrayList<>();
		final Map<Long, CouponUser> finalCouponUserMap = couponUserMap;
		final Map<Long, String> finalCouponNameMap = couponNameMap;
		for (CartItem cartItem : cartItems) {
			//校验
			Product product = productMap.get(cartItem.getProductId());
			if (product == null || product.getStatus() == 0) {
				throw new BusinessException("该商品已下架");
			}
			// 库存校验 + 扣减（抽出去了）
			deductStock(cartItem, product, specMap);
			// 组装订单项快照
			OrderItem item = new OrderItem();
			item.setProductId(product.getId());
			item.setProductName(product.getName());
			item.setCoverUrl(product.getCoverUrl());
			item.setPrice(product.getPrice());
			item.setQuantity(cartItem.getQuantity());
			if (cartItem.getSpecId() != null) {
				item.setSpecName(specMap.get(cartItem.getSpecId()).getSpecName());
			}
			//填充促销活动信息
			Promotion best = promotionService.findBestPromotion(product, activePromotions);
			if (best != null) {
				item.setPromotionName(best.getName());
				item.setPromotionalPrice(promotionService.calcDiscountedPrice(product.getPrice(), best));
			}
			//填充商品券信息
			if (productCouponMap != null && productCouponMap.get(cartItem.getId()) != null) {
				CouponUser cu = finalCouponUserMap.get(productCouponMap.get(cartItem.getId()));
				if (cu != null) {
					item.setCouponName(finalCouponNameMap.get(cu.getCouponId()));
					item.setCouponDiscount(productCouponDiscountMap.get(cartItem.getId()));
				}
			}
			orderItems.add(item);
		}

		// 生成订单号
		String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
				+ String.format("%06d", new Random().nextInt(999999));
		// 保存订单
		Order order = new Order();
		order.setUserId(userId);
		order.setOrderNo(orderNo);
		order.setStatus(1);
		order.setPayStatus(0);
		BeanUtils.copyProperties(dto, order);
		BeanUtils.copyProperties(previewVO, order);
		//保存订单券名称
		if (orderCoupon != null) {
			order.setOrderCouponName(orderCoupon.getName());
		}

		save(order);
		//把order存入数据库后把orderId回填到item里面
		orderItems.forEach(item -> item.setOrderId(order.getId()));

		// 核销订单券
		if (orderCouponUser != null) {
			orderCouponUser.setStatus(1);
			orderCouponUser.setUsedAt(LocalDateTime.now());
			orderCouponUser.setOrderId(order.getId());
			couponUserService.updateById(orderCouponUser);
		}

		// 核销商品券
		// 核销商品券：直接用 couponUserMap，不再循环查库
		if (dto.getProductCouponMap() != null) {
			for (Long userCouponId : dto.getProductCouponMap().values()) {
				CouponUser cu = couponUserMap.get(userCouponId);
				if (cu != null) {
					cu.setStatus(1);
					cu.setUsedAt(LocalDateTime.now());
					cu.setOrderId(order.getId());
					couponUserService.updateById(cu);
				}
			}
		}

		//批量保存订单项
		orderItemService.saveBatch(orderItems);
		//清理购物车
		cartItemService.removeByIds(dto.getCartItemIds());

		// 发送延迟消息（带 Confirm 重试，防止消息丢失）
		rabbitMQConfig.sendOrderTimeoutMessage(order.getId());
		return orderNo;
	}

	// 库存校验 + 数据库扣减
	private void deductStock(CartItem cartItem, Product product, Map<Long, ProductSpec> specMap) {
		int quantity = cartItem.getQuantity();
		if (cartItem.getSpecId() != null) {
			ProductSpec spec = specMap.get(cartItem.getSpecId());
			if (spec == null) {
				throw new BusinessException("Product spec not found");
			}
			boolean specUpdated = productSpecService.lambdaUpdate()
					.eq(ProductSpec::getId, spec.getId())
					.ge(ProductSpec::getStock, quantity)
					.setSql("stock = stock - " + quantity)
					.update();
			if (!specUpdated) {
				throw new BusinessException("Product spec stock is not enough");
			}
		}

		boolean productUpdated = productService.lambdaUpdate()
				.eq(Product::getId, product.getId())
				.ge(Product::getStock, quantity)
				.setSql("stock = stock - " + quantity)
				.update();
		if (!productUpdated) {
			throw new BusinessException("Product stock is not enough");
		}
		if (product.getStock() != null && product.getStock() - quantity == 0) {
			clearHomeSectionAllCache();
			productService.lambdaUpdate()
					.eq(Product::getId, product.getId())
					.eq(Product::getStock, 0)
					.set(Product::getStatus, 2)
					.update();
		}
	}

	@Override
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

		// 4. 并行查 Review 和 ReturnOrder
		List<Long> allItemIds = allItems.stream().map(OrderItem::getId).toList();

		Map<Long, Review> reviewMap;
		Map<Long, ReturnOrder> returnOrderMap;

		if (allItemIds.isEmpty()) {
			reviewMap = Map.of();
			returnOrderMap = Map.of();
		} else {
			ReviewAndReturnMap maps = fetchReviewAndReturnMap(allItemIds);
			reviewMap = maps.reviewMap();
			returnOrderMap = maps.returnOrderMap();
		}
		// 5. 组装 VO
		List<OrderVO> orderVoList = convertToVOList(orders, itemsGroupByOrderId, reviewMap, returnOrderMap);

		// 6. 返回PageResult
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

	//用户手动取消订单
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void cancelOrder(Long id, Long userId) {
		Order order = getById(id);
		if (order == null || !order.getUserId().equals(userId)) {
			throw new BusinessException("订单不存在");
		}
		if (order.getStatus() != OrderStatusConstants.PENDING_PAYMENT) {
			throw new BusinessException("只有待付款的订单可以取消");
		}
		if (!cancelOrderRollback(order)) {
			throw new BusinessException("订单状态已变更，无法取消");
		}
		// 秒杀订单回滚Redis购买记录（Lua保证原子性）
		if (order.getSeckillItemId() != null) {
			String stockKey = RedisSeckillConstants.SECKILL_PRODUCT_STOCK + order.getSeckillItemId();
			String userKey = RedisSeckillConstants.SECKILL_PRODUCT_USERS + order.getSeckillItemId();
			redisTemplate.execute(
					SECKILL_ROLLBACK_LUA_SCRIPT,
					Arrays.asList(stockKey, userKey),
					String.valueOf(order.getUserId())
			);
		}
	}

	//mq异步处理超时订单
	@Transactional(rollbackFor = Exception.class)
	public void cancelOrderByTimeout(Long orderId) {
		Order order = getById(orderId);
		// 只处理待付款的订单
		if (order == null || order.getStatus() != OrderStatusConstants.PENDING_PAYMENT) {
			return;
		}
		// 回滚（内含乐观锁，防止并发支付覆盖）
		if (!cancelOrderRollback(order)) {
			log.warn("[超时取消] 订单状态已变更，跳过回滚 orderId={}", orderId);
			return;
		}
		// 回滚Redis购买记录（Lua保证原子性），与consumer中回滚逻辑对齐
		if (order.getSeckillItemId() != null) {
			String stockKey = RedisSeckillConstants.SECKILL_PRODUCT_STOCK + order.getSeckillItemId();
			String userKey = RedisSeckillConstants.SECKILL_PRODUCT_USERS + order.getSeckillItemId();
			redisTemplate.execute(
					SECKILL_ROLLBACK_LUA_SCRIPT,
					Arrays.asList(stockKey, userKey),
					String.valueOf(order.getUserId())
			);
		}
	}

	private boolean cancelOrderRollback(Order order){
		// 乐观锁更新订单状态，防止并发支付覆盖
		boolean updated = lambdaUpdate()
				.eq(Order::getId, order.getId())
				.eq(Order::getStatus, OrderStatusConstants.PENDING_PAYMENT)
				.set(Order::getStatus, OrderStatusConstants.CANCELLED)
				.update();
		if (!updated) {
			return false; // 订单状态已被支付回调修改，终止回滚
		}

		List<OrderItem> items = orderItemService.lambdaQuery()
				.eq(OrderItem::getOrderId, order.getId())
				.list();
		// 回滚库存
		for (OrderItem item : items) {
			if (order.getSeckillItemId() != null) {
				// 秒杀订单只还seckill_stock
				seckillItemService.lambdaUpdate()
						.eq(SeckillItem::getId, order.getSeckillItemId())
						.setSql("seckill_stock = seckill_stock + 1")
						.update();
			} else {
				// 普通订单处理product.stock 和 spec.stock
				productService.lambdaUpdate()
						.eq(Product::getId, item.getProductId())
						.setSql("stock = stock + " + item.getQuantity())
						.update();
				if (item.getSpecName() != null) {
					productSpecService.lambdaUpdate()
							.eq(ProductSpec::getProductId, item.getProductId())
							.eq(ProductSpec::getSpecName, item.getSpecName())
							.setSql("stock = stock + " + item.getQuantity())
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

		// 发送消息给管理员
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String message = JSON.toJSONString(Map.of(
				"type", "CANCEL_ORDER",
				"orderId", order.getId(),
				"amount", order.getFinalAmount(),
				"time", LocalDateTime.now().format(formatter)
		));
		sessionManager.broadcast(message);
		return true;
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

		// 4. 逐个校验商品状态以及库存
		for (OrderItem item : orderItems) {
			Product product = productService.getById(item.getProductId());
			if (product == null || !product.getStatus() .equals(ProductStatusConstant.LISTED) ) {
				throw new BusinessException("商品【" + item.getProductName() + "】已下架");
			}
			if (product.getStock() < item.getQuantity()) {
				throw new BusinessException("商品【" + item.getProductName() + "】库存不足");
			}

			//校验规格库存
			ProductSpec rebuySpec = resolveRebuySpec(item, product);
			int availableStock = rebuySpec != null ? rebuySpec.getStock() : product.getStock();
			if (availableStock < item.getQuantity()) {
				String stockError = rebuySpec != null
						? "商品【" + item.getProductName() + "】的【" + rebuySpec.getSpecName() + "】规格库存不足"
						: "商品【" + item.getProductName() + "】库存不足";
				throw new BusinessException(stockError);
			}
			// 5. 加入购物车（已存在则数量叠加）
			CartItem cartItem = cartItemService.lambdaQuery()
					.eq(CartItem::getUserId, userId)
					.eq(CartItem::getProductId, item.getProductId())
					.eq(CartItem::getIsTemporary, 0)
					.eq(rebuySpec != null, CartItem::getSpecId, rebuySpec != null ? rebuySpec.getId() : null)
					.isNull(rebuySpec == null, CartItem::getSpecId)
					.one();
			if (cartItem != null) {
				// 已在购物车中，数量叠加
				cartItem.setQuantity(Math.min(cartItem.getQuantity() + item.getQuantity(), availableStock));
				cartItemService.updateById(cartItem);
			} else {
				// 不在购物车，新增
				CartItem newCartItem = new CartItem();
				newCartItem.setUserId(userId);
				newCartItem.setProductId(item.getProductId());
				newCartItem.setQuantity(item.getQuantity());
				newCartItem.setSpecId(rebuySpec != null ? rebuySpec.getId() : null);
				newCartItem.setIsTemporary(0);
				cartItemService.save(newCartItem);
			}
		}
	}

	//解决再次购买的规格bug
	private ProductSpec resolveRebuySpec(OrderItem item, Product product) {
		if (!Objects.equals(product.getHasSpec(), 1)) {
			return null;
		}
		if (item.getSpecName() == null || item.getSpecName().isBlank()) {
			throw new BusinessException("商品【" + item.getProductName() + "】缺少历史规格信息，无法再次购买");
		}

		ProductSpec spec = productSpecService.lambdaQuery()
				.eq(ProductSpec::getProductId, item.getProductId())
				.eq(ProductSpec::getSpecName, item.getSpecName())
				.one();
		if (spec == null) {
			throw new BusinessException("商品【" + item.getProductName() + "】的规格已变更，无法再次购买");
		}
		return spec;
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
		//1.并行查购物车项和订单券
		CompletableFuture<List<CartItem>> cartFuture = CompletableFuture.supplyAsync(
				() -> cartItemService.listByIds(dto.getCartItemIds()), executor);

		CompletableFuture<Coupon> orderCouponFuture = dto.getOrderUserCouponId() == null
				? CompletableFuture.completedFuture(null)
				: CompletableFuture.supplyAsync(() -> {
			CouponUser cu = couponUserService.getById(dto.getOrderUserCouponId());
			if (cu == null || !cu.getUserId().equals(userId)
					|| cu.getStatus() != 0
					|| cu.getExpireAt().isBefore(LocalDateTime.now())) return null;
			Coupon c = couponService.getById(cu.getCouponId());
			return (c != null && c.getCouponType() == 2) ? c : null;
		}, executor);

		CompletableFuture.allOf(cartFuture, orderCouponFuture).join();
		Coupon orderCoupon = orderCouponFuture.join();
		List<CartItem> cartItems = cartFuture.join();

		Map<Long, CartItem> cartItemMap = cartItems.stream()
				.collect(Collectors.toMap(CartItem::getId, c -> c));

		//2.查商品和分类
		List<Long> productIds = cartItems.stream().map(CartItem::getProductId).toList();
		List<Product> products = productService.listByIds(productIds);
		Map<Long, Product> productMap = products.stream()
				.collect(Collectors.toMap(Product::getId, p -> p));
		List<Long> categoryIds = products.stream().map(Product::getCategoryId).distinct().toList();

		//3.查促销活动
		List<Promotion> promotionList = promotionService.getActivePromotionList(productIds, categoryIds);

		//4.计算订单的原始总价和活动优惠的价格
		BigDecimal originalAmount = BigDecimal.ZERO;
		BigDecimal promotionDiscount = BigDecimal.ZERO;
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

		//5.计算商品券优惠
		BigDecimal productCouponDiscount = BigDecimal.ZERO;
		Map<Long, BigDecimal> productCouponDiscountMap = new HashMap<>();
		if (dto.getProductCouponMap() != null && !dto.getProductCouponMap().isEmpty()) {
			for (Map.Entry<Long, Long> entry : dto.getProductCouponMap().entrySet()) {
				//查询每个订单项是否使用了商品券
				CartItem item = cartItemMap.get(entry.getKey());
				if (item == null) continue;
				CouponUser cu = couponUserService.getById(entry.getValue());
				if (cu == null || !cu.getUserId().equals(userId)
						|| cu.getStatus() != 0
						|| cu.getExpireAt().isBefore(LocalDateTime.now())) continue;
				Coupon coupon = couponService.getById(cu.getCouponId());
				if (coupon == null || coupon.getCouponType() != 1) continue;
				Product product = productMap.get(item.getProductId());
				if (product == null) continue;

				// 校验适用范围是否匹配
				boolean applicable = switch (coupon.getScope()) {
					case 1 -> true; //全场,可以用
					case 2 -> product.getCategoryId().equals(coupon.getScopeId());//看分类是否匹配
					case 3 -> product.getId().equals(coupon.getScopeId());//看商品是否匹配
					default -> false;
				};
				if (!applicable) continue;

				// 计算商品券优惠的价格
				BigDecimal discount = switch (coupon.getDiscountType()) {
					case 1 -> product.getPrice().compareTo(coupon.getMinAmount()) >= 0
							? coupon.getDiscountAmount() : null; //满减
					case 2 -> product.getPrice().subtract(
							product.getPrice().multiply(coupon.getDiscountRate())
									.setScale(2, RoundingMode.HALF_UP));//折扣
					case 3 -> coupon.getDiscountAmount();//无门槛
					default -> null;
				};
				if (discount == null) continue;

				productCouponDiscount = productCouponDiscount.add(discount);
				productCouponDiscountMap.put(item.getId(), discount);
			}
		}
		//计算商品券优惠后的价格
		BigDecimal afterProductCoupon = afterPromotion.subtract(productCouponDiscount);

		//6.组装VO的订单券以外的部分
		OrderPreviewVO vo = new OrderPreviewVO();
		vo.setOriginalAmount(originalAmount);
		vo.setPromotionTotalDiscount(promotionDiscount);
		vo.setProductCouponTotalDiscount(productCouponDiscount);
		vo.setProductCouponDiscountMap(productCouponDiscountMap);

		//7.分情况计算最终价格 并组装VO
		//(1)如果没有使用订单券
		if (orderCoupon == null) {
			vo.setOrderCouponDiscount(null);
			vo.setOrderCouponUsable(dto.getOrderUserCouponId() == null ? null : false);
			vo.setOrderCouponUsableReason(dto.getOrderUserCouponId() != null ? "此订单券不存在或已使用或已过期" : null);
			vo.setFinalAmount(afterProductCoupon.max(BigDecimal.ZERO));
			return vo;
		}
		// (2)如果已经使用了优惠活动,则不能给再使用订单券
		if (promotionDiscount.compareTo(BigDecimal.ZERO) > 0) {
			vo.setOrderCouponUsable(false);
			vo.setOrderCouponUsableReason("此订单券不能与该订单的促销活动叠加使用");
			vo.setFinalAmount(afterProductCoupon.max(BigDecimal.ZERO));
			return vo;
		}

		//(3)如果使用了订单券
		BigDecimal orderCouponDiscount = switch (orderCoupon.getDiscountType()) {
			case 2 -> afterProductCoupon.subtract(
					afterProductCoupon.multiply(orderCoupon.getDiscountRate())
							.setScale(2, RoundingMode.HALF_UP));
			case 3 -> orderCoupon.getDiscountAmount();
			default -> null;
		};
		if (orderCouponDiscount == null) {
			vo.setOrderCouponUsable(false);
			vo.setFinalAmount(afterProductCoupon.max(BigDecimal.ZERO));
			return vo;
		}
		vo.setOrderCouponDiscount(orderCouponDiscount);
		vo.setOrderCouponUsable(true);
		vo.setFinalAmount(afterProductCoupon.subtract(orderCouponDiscount).max(BigDecimal.ZERO));

		//8.返回vo
		return vo;
	}

	private OrderVO convertToVO(Order order) {
		OrderVO vo = new OrderVO();
		BeanUtils.copyProperties(order, vo);
		vo.setStatusDesc(getStatusDesc(order.getStatus()));

		//批量获取订单项
		List<OrderItem> items = orderItemService.lambdaQuery()
				.eq(OrderItem::getOrderId, order.getId())
				.list();

		List<Long> itemIds = items.stream().map(OrderItem::getId).collect(Collectors.toList());

		if (itemIds.isEmpty()) {
			vo.setItems(Collections.emptyList());
			vo.setTotalQuantity(0);
			return vo;
		}

		// 并行查 review 和 returnOrder
		ReviewAndReturnMap maps = fetchReviewAndReturnMap(itemIds);
		Map<Long, Review> reviewMap = maps.reviewMap();
		Map<Long, ReturnOrder> returnMap = maps.returnOrderMap();

		// 组装 ItemVO
		List<OrderItemVO> itemVOs = items.stream()
				.map(item -> convertToItemVO(item, reviewMap, returnMap))
				.toList();

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

			//组装 ItemVO
			List<OrderItemVO> orderItemVOs = items.stream()
					.map(item -> convertToItemVO(item, reviewMap, returnOrderMap))
					.toList();

			vo.setItems(orderItemVOs);
			vo.setTotalQuantity(items.stream().mapToInt(OrderItem::getQuantity).sum());
			return vo;
		}).toList();
	}

	private OrderItemVO convertToItemVO(OrderItem item,
	                                    Map<Long, Review> reviewMap,
	                                    Map<Long, ReturnOrder> returnOrderMap) {
		OrderItemVO itemVO = new OrderItemVO();
		BeanUtils.copyProperties(item, itemVO);

		//价格
		BigDecimal originalAmount = item.getPrice()
				.multiply(BigDecimal.valueOf(item.getQuantity()));
		BigDecimal promotionDiscount = item.getPromotionalPrice() != null
				? originalAmount.subtract(item.getPromotionalPrice()
				.multiply(BigDecimal.valueOf(item.getQuantity())))
				: BigDecimal.ZERO;
		BigDecimal coupon = Optional.ofNullable(item.getCouponDiscount())
				.orElse(BigDecimal.ZERO);

		itemVO.setOriginalAmount(originalAmount);
		itemVO.setPromotionDiscount(promotionDiscount);
		itemVO.setFinalAmount(originalAmount.subtract(promotionDiscount).subtract(coupon));

		if (item.getSeckillPrice() != null) {
			itemVO.setFinalAmount(item.getSeckillPrice()
					.multiply(BigDecimal.valueOf(item.getQuantity())));
		}

		//评论
		Review review = reviewMap.get(item.getId());
		itemVO.setReviewed(review != null);
		if (review != null) {
			itemVO.setReviewId(review.getId());
		}

		//退货
		ReturnOrder returnOrder = returnOrderMap.get(item.getId());
		if (returnOrder != null) {
			ReturnOrderVO returnOrderVO = new ReturnOrderVO();
			BeanUtils.copyProperties(returnOrder, returnOrderVO);
			returnOrderVO.setImages(returnOrder.getImages() != null
					? Arrays.asList(returnOrder.getImages().split(","))
					: null);
			returnOrderVO.setStatusDesc(getReturnStatusDesc(returnOrder.getStatus()));
			itemVO.setReturnOrder(returnOrderVO);
		}

		return itemVO;
	}

	// 简单用record包装返回值
	private record ReviewAndReturnMap(
			Map<Long, Review> reviewMap,
			Map<Long, ReturnOrder> returnOrderMap
	) {}

	private ReviewAndReturnMap fetchReviewAndReturnMap(List<Long> itemIds) {
		// 并行查 review 和 returnOrder
		CompletableFuture<Map<Long, Review>> reviewFuture = CompletableFuture.supplyAsync(() ->
						reviewService.lambdaQuery()
								.in(Review::getOrderItemId, itemIds).list()
								.stream()
								.collect(Collectors.toMap(Review::getOrderItemId, r -> r))
				, executor);

		CompletableFuture<Map<Long, ReturnOrder>> returnFuture = CompletableFuture.supplyAsync(() ->
						returnOrderService.lambdaQuery()
								.in(ReturnOrder::getOrderItemId, itemIds).list()
								.stream()
								.collect(Collectors.toMap(ReturnOrder::getOrderItemId, r -> r))
				, executor);

		// join()会阻塞当前线程，直到两个Future都完成
		CompletableFuture.allOf(reviewFuture, returnFuture).join();
		// 取结果（join()不会抛检查异常,可以不写 try-catch,而get()必须写try-catch）
		return new ReviewAndReturnMap(reviewFuture.join(), returnFuture.join());
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

	/**
	 * 兜底：每5分钟扫描创建超过15分钟且仍待付款的订单，执行超时取消。
	 * MQ 延迟消息可能因 Broker 故障或 Confirm nack 丢失，此定时任务保证最终一致性。
	 */
	@Scheduled(fixedDelay = 300000)
	public void scanTimeoutOrders() {
		List<Order> timeoutOrders = lambdaQuery()
				.eq(Order::getStatus, OrderStatusConstants.PENDING_PAYMENT)
				.lt(Order::getCreatedAt, LocalDateTime.now().minusMinutes(15))
				.list();
		if (timeoutOrders.isEmpty()) return;
		log.info("[定时扫描] 发现{}笔超时待付款订单，开始取消", timeoutOrders.size());
		for (Order order : timeoutOrders) {
			try {
				cancelOrderByTimeout(order.getId());
			} catch (Exception e) {
				log.error("[定时扫描] 超时订单取消失败 orderId={}", order.getId(), e);
			}
		}
	}
}
