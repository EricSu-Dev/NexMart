package com.nex.nexmart.service.impl.seckill;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nex.nexmart.rabbit.mq.message.SeckillProductOrderMessage;
import com.nex.nexmart.common.constant.RabbitMQConstants;
import com.nex.nexmart.common.constant.RedisSeckillConstants;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.CouponUserMapper;
import com.nex.nexmart.mapper.base.CouponMapper;
import com.nex.nexmart.rabbit.mq.message.SeckillCouponMessage;
import com.nex.nexmart.model.dto.seckill.SeckillProductOrderDTO;
import com.nex.nexmart.model.entity.Address;
import com.nex.nexmart.model.entity.coupon.CouponUser;
import com.nex.nexmart.model.entity.seckill.SeckillActivity;
import com.nex.nexmart.model.entity.seckill.SeckillItem;
import com.nex.nexmart.model.entity.coupon.Coupon;
import com.nex.nexmart.model.entity.order.Order;
import com.nex.nexmart.model.entity.order.OrderItem;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.entity.product.ProductSpec;
import com.nex.nexmart.service.intf.AddressService;
import com.nex.nexmart.service.intf.seckill.SeckillActivityService;
import com.nex.nexmart.service.intf.seckill.SeckillItemService;
import com.nex.nexmart.service.intf.seckill.SeckillOrderService;
import com.nex.nexmart.service.intf.order.OrderItemService;
import com.nex.nexmart.service.intf.order.OrderService;
import com.nex.nexmart.service.intf.product.ProductService;
import com.nex.nexmart.service.intf.product.ProductSpecService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SeckillOrderServiceImpl implements SeckillOrderService {

	private final SeckillItemService seckillItemService;
	private final SeckillActivityService seckillActivityService;
	private final ProductService productService;
	private final ProductSpecService productSpecService;
	private final OrderService orderService;
	private final OrderItemService orderItemService;
	private final AddressService addressService;
	private final CouponMapper couponMapper;
	private final RabbitTemplate rabbitTemplate;
	private final CouponUserMapper couponUserMapper;
	private final RedisTemplate<String, String> redisTemplate;

	@Override
	@Transactional
	public void createCouponOrder(Long userId, Long seckillItemId) {
		// 1. 查 seckill_item，校验 status
		SeckillItem item = seckillItemService.getById(seckillItemId);
		if (item == null || item.getStatus() != 1) {
			throw new BusinessException("秒杀券不存在或已下架");
		}

		// 2. 查 seckill_activity，校验 status + phase
		SeckillActivity activity = seckillActivityService.getById(item.getActivityId());
		LocalDateTime now = LocalDateTime.now();
		if (activity == null || activity.getStatus() != 1
				|| now.isBefore(activity.getStartTime())
				|| now.isAfter(activity.getEndTime())) {
			throw new BusinessException("活动不存在或未上架或未在进行中");
		}

		// 3. Redis库存预热（懒加载）
		String stockKey = RedisSeckillConstants.SECKILL_COUPON_STOCK + seckillItemId;
		String userKey = RedisSeckillConstants.SECKILL_COUPON_USERS + seckillItemId;

		//4. 查coupon + 校验
		Coupon coupon = couponMapper.selectById(item.getCouponId());
		if (coupon == null || coupon.getCouponType() != 2 || coupon.getStatus() != 1) {
			throw new BusinessException("券不存在或未上架");
		}
		//redis没有数据则存入
		if (!Boolean.TRUE.equals(redisTemplate.hasKey(stockKey))) {
			redisTemplate.opsForValue().setIfAbsent(
					stockKey,
					String.valueOf(coupon.getRemained())
			);
		}
		if (!Boolean.TRUE.equals(redisTemplate.hasKey(userKey))) {
			long count = couponUserMapper.countByUserAndCoupon(userId, coupon.getId());
			redisTemplate.opsForHash().put(userKey, String.valueOf(userId), String.valueOf(count));
		}

		// 5.Lua执行
		Long result = redisTemplate.execute(
				new DefaultRedisScript<>(RedisSeckillConstants.SECKILL_LUA, Long.class),
				Arrays.asList(stockKey, userKey),
				String.valueOf(userId),
				String.valueOf(coupon.getPerLimit())
		);

		if (result == null || result == -1L) {
			throw new BusinessException("券库存不足");
		}
		if (result == -2L) {
			throw new BusinessException("已达限购上限");
		}

		// 6. 发MQ，异步处理写DB
		SeckillCouponMessage msg = new SeckillCouponMessage();
		msg.setUserId(userId);
		msg.setSeckillItemId(seckillItemId);
		try {
			rabbitTemplate.convertAndSend(
					RabbitMQConstants.SECKILL_ORDER_EXCHANGE,
					RabbitMQConstants.SECKILL_COUPON_ORDER_ROUTING_KEY,
					msg
			);
		}catch (Exception e) {
			// 只有发MQ失败才在这里回滚Redis
			redisTemplate.opsForValue().increment(stockKey);
			redisTemplate.opsForHash().increment(userKey, String.valueOf(userId), -1);
			throw new BusinessException("系统繁忙，请重试");
		}
	}

	@Transactional
	public void createCouponOrderAsync(Long userId, Long seckillItemId) {
		SeckillItem item = seckillItemService.getById(seckillItemId);
		Coupon coupon = couponMapper.selectById(item.getCouponId());
		LocalDateTime now = LocalDateTime.now();

		// 扣DB库存
		int rows = couponMapper.update(null,
				new LambdaUpdateWrapper<Coupon>()
						.eq(Coupon::getId, coupon.getId())
						.gt(Coupon::getRemained, 0)
						.setSql("remained = remained - 1")
		);
		if (rows == 0) {
			throw new BusinessException("券库存不足");
		}

		// 写coupon_user
		CouponUser couponUser = new CouponUser();
		couponUser.setUserId(userId);
		couponUser.setCouponId(coupon.getId());
		couponUser.setCouponType(2);
		couponUser.setStatus(0);
		couponUser.setExpireAt(now.plusDays(coupon.getValidDays()));
		couponUserMapper.insert(couponUser);
	}

	@Override
	@Transactional
	public void createProductOrder(Long userId, SeckillProductOrderDTO dto) {

		// 1. 查 seckill_item，校验 status
		SeckillItem item = seckillItemService.getById(dto.getSeckillItemId());
		if (item == null || item.getStatus() != 1) {
			throw new BusinessException("秒杀商品不存在或已下架");
		}

		// 2. 查 seckill_activity，校验 status + phase
		SeckillActivity activity = seckillActivityService.getById(item.getActivityId());
		LocalDateTime now = LocalDateTime.now();
		if (activity == null || activity.getStatus() != 1
				|| now.isBefore(activity.getStartTime())
				|| now.isAfter(activity.getEndTime())) {
			throw new BusinessException("活动不存在或未在进行中");
		}

		// 3. 预热
		String stockKey = RedisSeckillConstants.SECKILL_PRODUCT_STOCK + dto.getSeckillItemId();
		String userKey = RedisSeckillConstants.SECKILL_PRODUCT_USERS + dto.getSeckillItemId();

		// 4. 查商品+校验
		Product product = productService.getById(item.getProductId());
		if (product == null) {
			throw new BusinessException("商品不存在");
		}
		if (!Boolean.TRUE.equals(redisTemplate.hasKey(stockKey))) {
			redisTemplate.opsForValue().setIfAbsent(stockKey, String.valueOf(item.getSeckillStock()));
		}
		if (!Boolean.TRUE.equals(redisTemplate.hasKey(userKey))) {
			Long count = orderService.lambdaQuery()
					.eq(Order::getUserId, userId)
					.eq(Order::getSeckillItemId, dto.getSeckillItemId())
					.ne(Order::getStatus, 0) // 排除已取消
					.count();
			redisTemplate.opsForHash().put(userKey, String.valueOf(userId), String.valueOf(count));
		}


		// 5. 有规格校验 sku —— 直接从 item 取 productSpecId，不需要用户传
		ProductSpec spec = null;
		if (product.getHasSpec() == 1) {
			if (item.getProductSpecId() == null) {
				throw new BusinessException("秒杀商品未绑定规格");
			}
			spec = productSpecService.getById(item.getProductSpecId());
			if (spec == null) {
				throw new BusinessException("规格不存在");
			}
		}

		// 6.Lua执行
		Long result = redisTemplate.execute(
				new DefaultRedisScript<>(RedisSeckillConstants.SECKILL_LUA, Long.class),
				Arrays.asList(stockKey, userKey),
				String.valueOf(userId),
				String.valueOf(item.getPerLimit())  // perLimit从item取
		);

		if (result == null || result == -1L) {
			throw new BusinessException("秒杀库存不足");
		}
		if (result == -2L) {
			throw new BusinessException("已达限购上限");
		}

		// 7.发MQ，异步处理写DB
		SeckillProductOrderMessage msg = new SeckillProductOrderMessage();
		msg.setUserId(userId);
		msg.setSeckillItemId(dto.getSeckillItemId());
		msg.setSkuId(item.getProductSpecId());
		msg.setAddressId(dto.getAddressId());
		try {
			rabbitTemplate.convertAndSend(
					RabbitMQConstants.SECKILL_ORDER_EXCHANGE,
					RabbitMQConstants.SECKILL_PRODUCT_ORDER_ROUTING_KEY,
					msg
			);
		}catch (Exception e) {
			// 只有发MQ失败才在这里回滚Redis
			redisTemplate.opsForValue().increment(stockKey);
			redisTemplate.opsForHash().increment(userKey, String.valueOf(userId), -1);
			throw new BusinessException("系统繁忙，请重试");
		}
	}

	@Transactional
	public void createProductOrderAsync(SeckillProductOrderMessage msg) {
		Long userId = msg.getUserId();
		SeckillItem item = seckillItemService.getById(msg.getSeckillItemId());
		SeckillActivity activity = seckillActivityService.getById(item.getActivityId());
		Product product = productService.getById(item.getProductId());

		// 1. 扣秒杀库存
		boolean seckillStockSuccess = seckillItemService.lambdaUpdate()
				.eq(SeckillItem::getId, item.getId())
				.gt(SeckillItem::getSeckillStock, 0)
				.setSql("seckill_stock = seckill_stock - 1")
				.update();
		if (!seckillStockSuccess) {
			throw new BusinessException("秒杀库存不足");
		}

		// 2. 查规格（绑定秒杀活动已预占了库存，不再扣）
		ProductSpec spec = null;
		if (msg.getSkuId() != null) {
			spec = productSpecService.getById(msg.getSkuId());
		}

		// 3. 查收货地址
		Address address = addressService.getById(msg.getAddressId());

		// 4. 生成订单
		String orderNo = "SK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
				+ String.format("%06d", new Random().nextInt(999999));
		Order order = new Order();
		order.setOrderNo(orderNo);
		order.setUserId(userId);
		order.setAddressId(address.getId());
		order.setStatus(1);
		order.setPayStatus(0);
		order.setSeckillItemId(item.getId());
		BigDecimal originalAmount = product.getPrice();
		BigDecimal seckillPrice = item.getSeckillPrice();
		order.setOriginalAmount(originalAmount);
		order.setSeckillDiscount(originalAmount.subtract(seckillPrice));
		order.setFinalAmount(seckillPrice);
		order.setReceiverName(address.getReceiverName());
		order.setReceiverPhone(address.getReceiverPhone());
		order.setAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
		order.setPromotionTotalDiscount(null);
		order.setProductCouponTotalDiscount(null);
		order.setOrderCouponDiscount(null);
		order.setOrderCouponName(null);
		orderService.save(order);

		// 5. 生成订单项
		OrderItem orderItem = new OrderItem();
		orderItem.setOrderId(order.getId());
		orderItem.setProductId(product.getId());
		orderItem.setProductName(product.getName());
		orderItem.setPrice(originalAmount);
		orderItem.setSeckillPrice(seckillPrice);
		orderItem.setSeckillName(activity.getName());
		orderItem.setQuantity(1);
		orderItem.setSpecName(spec != null ? spec.getSpecName() : null);
		orderItem.setCoverUrl(product.getCoverUrl());
		orderItem.setPromotionalPrice(null);
		orderItem.setCouponDiscount(null);
		orderItem.setPromotionName(null);
		orderItem.setCouponName(null);
		orderItemService.save(orderItem);

		// 6. 发超时取消订单的MQ
		Map<String, Object> timeoutMsg = new HashMap<>();
		timeoutMsg.put("orderId", order.getId());
		rabbitTemplate.convertAndSend(
				RabbitMQConstants.ORDER_DELAY_EXCHANGE,
				RabbitMQConstants.ORDER_DELAY_ROUTING_KEY,
				timeoutMsg
		);
	}
}
