package com.nex.nexmart.service.impl.seckill;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.common.constant.RedisSeckillConstants;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.CouponUserMapper;
import com.nex.nexmart.mapper.OrderMapper;
import com.nex.nexmart.mapper.base.SeckillActivityMapper;
import com.nex.nexmart.model.dto.seckill.AddSeckillItemDTO;
import com.nex.nexmart.model.dto.seckill.BindSeckillItemDTO;
import com.nex.nexmart.model.entity.seckill.SeckillActivity;
import com.nex.nexmart.model.entity.seckill.SeckillItem;
import com.nex.nexmart.model.entity.coupon.Coupon;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.entity.product.ProductSpec;
import com.nex.nexmart.model.vo.seckill.SeckillCouponItemVO;
import com.nex.nexmart.model.vo.seckill.SeckillProductItemVO;
import com.nex.nexmart.service.intf.product.ProductService;
import com.nex.nexmart.service.intf.product.ProductSpecService;
import com.nex.nexmart.service.intf.seckill.SeckillItemService;
import com.nex.nexmart.mapper.SeckillItemMapper;
import com.nex.nexmart.service.intf.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author Eric
*  针对表【seckill_item(秒杀商品表)】的数据库操作Service实现
*  2026-04-13 16:10:37
*/
@Service
@RequiredArgsConstructor
public class SeckillItemServiceImpl extends ServiceImpl<SeckillItemMapper, SeckillItem> implements SeckillItemService {

	private final CouponService couponService;
	private final ProductService productService;
	private final SeckillActivityMapper activityMapper;
	private final OrderMapper orderMapper;
	private final CouponUserMapper couponUserMapper;
	private final ProductSpecService productSpecService;
	private final SeckillActivityMapper seckillActivityMapper;
	private final RedisTemplate<String,String> redisTemplate;

	@Override
	public PageResult<SeckillProductItemVO> productList(Integer current, Integer size, Boolean onlyUnbound,Long activityId) {
		Page<SeckillProductItemVO> pageParam = new Page<>(current, size);
		IPage<SeckillProductItemVO> IPage = baseMapper.productList(pageParam, onlyUnbound, activityId);
		return PageResult.of(IPage);
	}

	@Override
	public PageResult<SeckillCouponItemVO> couponList(Integer current, Integer size, Boolean onlyUnbound,Long activityId) {
		Page<SeckillCouponItemVO> pageParam = new Page<>(current, size);
		IPage<SeckillCouponItemVO> IPage = baseMapper.couponList(pageParam, onlyUnbound, activityId);
		return PageResult.of(IPage);
	}

	@Override
	@Transactional
	public void addItem(AddSeckillItemDTO dto) {
		// 校验
		if (dto.getItemType() == 1) {
			if (dto.getProductId() == null || dto.getSeckillPrice() == null) {
				throw new BusinessException("商品类型必须填写商品ID和秒杀价");
			}
			Product product = productService.getById(dto.getProductId());
			if(product == null){
				throw new BusinessException("商品不存在,请检查商品ID");
			}
			if(product.getStatus().equals(0)||product.getStatus().equals(2)){
				throw new BusinessException("商品已下架或售空,请先上架或添加库存");
			}
			if(product.getStock()< dto.getSeckillStock()){
				throw new BusinessException("秒杀库存不能大于商品库存");
			}
			if(product.getPrice().compareTo(dto.getSeckillPrice())<0){
				throw new BusinessException("秒杀价不能高于商品价格");
			}
			if(product.getHasSpec()==1){
				if (dto.getProductSpecId()== null) {
					throw new BusinessException("商品有规格,请选择商品规格");
				}
				ProductSpec productSpec = productSpecService.getById(dto.getProductSpecId());
				if (productSpec == null) {
					throw new BusinessException("商品规格不存在,请检查商品规格ID");
				}
				if(dto.getSeckillStock().compareTo(productSpec.getStock())>0){
					throw new BusinessException("秒杀库存不能大于商品规格库存");
				}
			}

			Long count = lambdaQuery().eq(SeckillItem::getProductId, dto.getProductId()).count();
			if (count > 0) {
				throw new BusinessException("该商品已添加到秒杀项中");
			}
		} else if (dto.getItemType() == 2) {
			if (dto.getCouponId() == null) {
				throw new BusinessException("优惠券类型必须填写券ID");
			}
			Coupon coupon = couponService.lambdaQuery().eq(Coupon::getId, dto.getCouponId()).one();
			if(coupon == null||coupon.getCouponType() != 2){
				throw new BusinessException("该订单券不存在或不是订单券");
			}
			if(coupon.getStatus().equals(0)){
				throw new BusinessException("该订单券已下架,请先上架");
			}
			if(coupon.getReceiveChannel()!=null){
				throw new BusinessException("该订单券已绑定领取渠道!");
			}
			//绑定渠道
			couponService.lambdaUpdate().eq(Coupon::getId, coupon.getId()).set(Coupon::getReceiveChannel,3).update();
		}

		SeckillItem item = new SeckillItem();
		BeanUtils.copyProperties(dto, item);
		item.setSoldCount(0);
		item.setStatus(1);
		save(item);
	}

	@Override
	@Transactional
	public void removeItem(Long id) {
		// 进行中活动的商品不允许删除，按需加校验
		SeckillItem item = getById(id);
		if (item.getStatus().equals(2)) {
			throw new BusinessException("上架的商品不允许删除,请先下架");
		}
		removeById(id);
		//清除领取渠道
		couponService.lambdaUpdate().eq(Coupon::getId, item.getCouponId()).set(Coupon::getReceiveChannel,null).update();
	}

	@Override
	public void updateItem(Long id, BigDecimal seckillPrice,Integer perLimit){
		SeckillItem item = getById(id);
		if (item == null) throw new BusinessException("秒杀商品不存在");
		if(item.getActivityId()!=null){
			throw new BusinessException("已绑定活动商品不允许修改,请先解除绑定");
		}
		if(item.getStatus()==1){
			throw  new BusinessException("秒杀商品项已上架,请先下架");
		}
		Product product = productService.getById(item.getProductId());
		if(product == null){
			throw new BusinessException("商品不存在,请检查商品ID");
		}
		if(seckillPrice.compareTo(product.getPrice())>0){
			throw new BusinessException("秒杀价不能高于商品价格");
		}
		lambdaUpdate().eq(SeckillItem::getId, id)
				.set(SeckillItem::getSeckillPrice, seckillPrice)
				.set(SeckillItem::getPerLimit, perLimit)
				.update();
		redisTemplate.delete(RedisSeckillConstants.SECKILL_PRODUCT_LIST + item.getActivityId());
	}

	@Override
	public void updateStatus(Long id, Integer status) {
		SeckillItem item = getById(id);
		if (item == null) throw new BusinessException("秒杀商品不存在");
		item.setStatus(status);
		updateById(item);
		// 清除缓存
		if(item.getActivityId()!=null) {
			if (item.getItemType() == 1) {
				redisTemplate.delete(RedisSeckillConstants.SECKILL_PRODUCT_LIST + item.getActivityId());
			} else if (item.getItemType() == 2) {
				redisTemplate.delete(RedisSeckillConstants.SECKILL_COUPON_LIST + item.getActivityId());
			}
		}
	}




	@Override
	@Transactional
	public void bindActivity(BindSeckillItemDTO dto) {
		// 查活动时间
		SeckillActivity activity = activityMapper.selectById(dto.getActivityId());
		if (activity == null) throw new RuntimeException("活动不存在");

		if (!dto.getItemIds().isEmpty()) {
			List<SeckillItem> items = listByIds(dto.getItemIds());
			// 校验状态
			List<String> disabledItems = items.stream()
					.filter(i -> i.getStatus() == 2)
					.map(i -> String.valueOf(i.getId()))
					.toList();
			if (!disabledItems.isEmpty()) {
				throw new RuntimeException("以下秒杀项已下架，无法绑定：" + String.join(",", disabledItems));
			}
			List<String> hasBound = items.stream()
					.filter(i -> i.getActivityId() != null)
					.map(i -> String.valueOf(i.getId()))
					.toList();
			if (!hasBound.isEmpty()) {
				throw new RuntimeException("以下秒杀项已绑定其他活动，无法绑定：" + String.join(",", hasBound));
			}

			if(activity.getActivityType()==1){
				List<String> couponActivity = items.stream().filter(i -> i.getItemType() == 2)
						.map(i -> String.valueOf(i.getCouponId()))
						.toList();
				if(!couponActivity.isEmpty()){
					throw new RuntimeException("秒杀项中包含以下订单券:"+String.join(",", couponActivity)+"，请选择商品秒杀项");
				}
			}

			if(activity.getActivityType()==2){
				List<String> productActivity = items.stream().filter(i -> i.getItemType() == 1)
						.map(i -> String.valueOf(i.getProductId()))
						.toList();
				if(!productActivity.isEmpty()){
					throw new RuntimeException("秒杀项中包含以下商品:"+String.join(",", productActivity)+"，请选择订单券秒杀项");
				}

				// 校验券类型的秒杀项时间是否合法
				List<Long> couponIds = items.stream()
						.filter(i -> i.getItemType() == 2)
						.map(SeckillItem::getCouponId)
						.collect(Collectors.toList());

				if (!couponIds.isEmpty()) {
					List<Coupon> coupons = couponService.listByIds(couponIds);
					for (Coupon coupon : coupons) {
						if (coupon.getReceiveEnd().isBefore(activity.getStartTime())) {
							throw new RuntimeException(
									"订单券【" + coupon.getName() + "】的领取截止时间早于活动开始时间，无法绑定"
							);
						}
					}
				}
			}
		}

		//  绑定选中的秒杀项
		if (!dto.getItemIds().isEmpty()) {
			lambdaUpdate()
					.set(SeckillItem::getActivityId, dto.getActivityId())
					.in(SeckillItem::getId, dto.getItemIds())
					.update();

			// 预占库存（仅商品类型）
			if (activity.getActivityType() == 1) {
				List<SeckillItem> items = lambdaQuery()
						.in(SeckillItem::getId, dto.getItemIds())
						.list();
				for (SeckillItem item : items) {
					if (item.getProductSpecId() != null) {
						// 有规格，扣 spec.stock 和 product.stock
						boolean success = productSpecService.lambdaUpdate()
								.eq(ProductSpec::getId, item.getProductSpecId())
								.setSql("stock = stock - " + item.getSeckillStock())
								.gt(ProductSpec::getStock, 0)
								.update();
						if (!success) {
							throw new BusinessException("商品ID为"+item.getProductId()+"的规格的库存不足，预占失败");
						}
						productService.lambdaUpdate()
								.eq(Product::getId, item.getProductId())
								.setSql("stock = stock - " + item.getSeckillStock())
								.update();
					} else {
						// 无规格，只扣 product.stock
						boolean success = productService.lambdaUpdate()
								.eq(Product::getId, item.getProductId())
								.setSql("stock = stock - " + item.getSeckillStock())
								.gt(Product::getStock, 0)
								.update();
						if (!success) {
							throw new BusinessException("商品ID为"+item.getProductId()+"的库存不足，预占失败");
						}
					}
				}
			}
			// 清除缓存
			if (activity.getActivityType() == 1) {
				redisTemplate.delete(RedisSeckillConstants.SECKILL_PRODUCT_LIST + dto.getActivityId());
			} else if (activity.getActivityType() == 2) {
				redisTemplate.delete(RedisSeckillConstants.SECKILL_COUPON_LIST + dto.getActivityId());
			}
		}
	}

	@Override
	@Transactional
	public void deleteBind(BindSeckillItemDTO dto) {
		SeckillActivity activity = activityMapper.selectById(dto.getActivityId());
		if (activity == null) throw new RuntimeException("活动不存在");
		if (!dto.getItemIds().isEmpty()) {

			// 归还库存（仅商品类型）
			if (activity.getActivityType() == 1) {
				List<SeckillItem> items = lambdaQuery()
						.in(SeckillItem::getId, dto.getItemIds())
						.list();
				for (SeckillItem item : items) {
					if (item.getSeckillStock() <= 0) continue;
					if (item.getProductSpecId() != null) {
						productSpecService.lambdaUpdate()
								.eq(ProductSpec::getId, item.getProductSpecId())
								.setSql("stock = stock + " + item.getSeckillStock())
								.update();
					}
					productService.lambdaUpdate()
							.eq(Product::getId, item.getProductId())
							.setSql("stock = stock + " + item.getSeckillStock())
							.update();
				}
			}

			lambdaUpdate()
					.set(SeckillItem::getActivityId, null)
					.in(SeckillItem::getId, dto.getItemIds())
					.update();

			// 清除缓存
			if (activity.getActivityType() == 1) {
				redisTemplate.delete(RedisSeckillConstants.SECKILL_PRODUCT_LIST + dto.getActivityId());
			} else if (activity.getActivityType() == 2) {
				redisTemplate.delete(RedisSeckillConstants.SECKILL_COUPON_LIST + dto.getActivityId());
			}
		}
	}

	@Override
	@SuppressWarnings("BusyWait")
	public List<SeckillProductItemVO> productListByActivity(Long activityId, Long userId) {
		String cacheKey = RedisSeckillConstants.SECKILL_PRODUCT_LIST + activityId;
		String lockKey = RedisSeckillConstants.SECKILL_PRODUCT_LIST_LOCK + activityId;
		String requestId = UUID.randomUUID().toString();
		while (true) {
			// 1. 查缓存
			String cached = redisTemplate.opsForValue().get(cacheKey);
			if (cached != null) {
				if ("[]".equals(cached)) return Collections.emptyList();
				List<SeckillProductItemVO> list = JSON.parseArray(cached, SeckillProductItemVO.class);
				fillProductPurchased(list, userId);
				return list;
			}

			// 2. 抢互斥锁
			Boolean locked = redisTemplate.opsForValue()
					.setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);

			if (!Boolean.TRUE.equals(locked)) {
				try { Thread.sleep(50); } catch (InterruptedException ignored) {}
				continue;
			}

			try {
				// 3. 双重检查
				cached = redisTemplate.opsForValue().get(cacheKey);
				if (cached != null) {
					if ("[]".equals(cached)) return Collections.emptyList();
					List<SeckillProductItemVO> list = JSON.parseArray(cached, SeckillProductItemVO.class);
					fillProductPurchased(list, userId);
					return list;
				}

				// 4. 查DB
				List<SeckillProductItemVO> list = baseMapper.productListByActivity(activityId, userId);

				// 5. 写缓存（不含purchased，TTL加随机值防雪崩）
				redisTemplate.opsForValue().set(
						cacheKey,
						JSON.toJSONString(list),
						5 + new Random().nextInt(3),
						TimeUnit.MINUTES
				);

				// 6. 实时填充purchased
				fillProductPurchased(list, userId);
				return list;
			} finally {
				redisTemplate.execute(new DefaultRedisScript<>(RedisSeckillConstants.RELEASE_LOCK_SCRIPT, Long.class),
						Collections.singletonList(lockKey), requestId);
			}
		}
	}



	@Override
	@SuppressWarnings("BusyWait")
	public List<SeckillCouponItemVO> couponListByActivity(Long activityId, Long userId) {
		String cacheKey = RedisSeckillConstants.SECKILL_COUPON_LIST + activityId;
		String lockKey = RedisSeckillConstants.SECKILL_COUPON_LIST_LOCK + activityId;
		String requestId = UUID.randomUUID().toString();

		while (true) {
			// 1. 查缓存
			String cached = redisTemplate.opsForValue().get(cacheKey);
			if (cached != null) {
				if ("[]".equals(cached)) return Collections.emptyList();
				List<SeckillCouponItemVO> list = JSON.parseArray(cached, SeckillCouponItemVO.class);
				fillCouponPurchased(list, userId);
				return list;
			}

			// 2. 抢互斥锁
			Boolean locked = redisTemplate.opsForValue()
					.setIfAbsent(lockKey, requestId, 10, TimeUnit.SECONDS);

			if (!Boolean.TRUE.equals(locked)) {
				try { Thread.sleep(50); } catch (InterruptedException ignored) {}
				continue;
			}

			try {
				// 3. 双重检查
				cached = redisTemplate.opsForValue().get(cacheKey);
				if (cached != null) {
					if ("[]".equals(cached)) return Collections.emptyList();
					List<SeckillCouponItemVO> list = JSON.parseArray(cached, SeckillCouponItemVO.class);
					fillCouponPurchased(list, userId);
					return list;
				}

				// 4. 查DB
				List<SeckillCouponItemVO> list = baseMapper.couponListByActivity(activityId, userId);
				list.removeIf(item -> {
					LocalDateTime now = LocalDateTime.now();
					return now.isBefore(item.getCouponReceiveStart())
							|| now.isAfter(item.getCouponReceiveEnd());
				});

				// 5. 写缓存（不含purchased，TTL加随机值防雪崩）
				redisTemplate.opsForValue().set(
						cacheKey,
						JSON.toJSONString(list),
						5 + new Random().nextInt(3),
						TimeUnit.MINUTES
				);

				// 6. 实时填充purchased
				fillCouponPurchased(list, userId);
				return list;
			} finally {
				redisTemplate.execute(new DefaultRedisScript<>(RedisSeckillConstants.RELEASE_LOCK_SCRIPT, Long.class),
						Collections.singletonList(lockKey), requestId);
			}
		}
	}

	// 抽取：实时填充purchased
	private void fillProductPurchased(List<SeckillProductItemVO> list, Long userId) {
		list.forEach(item -> {
			long bought = orderMapper.countByUserAndSeckillItem(userId, item.getId());
			item.setPurchased(bought >= item.getPerLimit());
		});
	}

	private void fillCouponPurchased(List<SeckillCouponItemVO> list, Long userId) {
		list.forEach(item -> {
			long owned = couponUserMapper.countByUserAndCoupon(userId, item.getCouponId());
			Coupon coupon = couponService.getById(item.getCouponId());
			item.setPurchased(owned >= coupon.getPerLimit());
		});
	}

	@Scheduled(fixedDelay = 60000) // 每分钟执行一次
	public void returnExpiredActivityStock() {
		// 查出所有已过期且未处理的活动
		List<SeckillActivity> expiredActivities = seckillActivityMapper.selectList(
				new LambdaQueryWrapper<SeckillActivity>()
						.eq(SeckillActivity::getStatus, 1)
						.eq(SeckillActivity::getActivityType, 1)
						.lt(SeckillActivity::getEndTime, LocalDateTime.now())
		);

		for (SeckillActivity activity : expiredActivities) {
			// 查出该活动下所有商品项
			List<SeckillItem> items = lambdaQuery()
					.eq(SeckillItem::getActivityId, activity.getId())
					.eq(SeckillItem::getItemType, 1)
					.list();

			for (SeckillItem item : items) {
				if (item.getSeckillStock() <= 0) continue;
				if (item.getProductSpecId() != null) {
					productSpecService.lambdaUpdate()
							.eq(ProductSpec::getId, item.getProductSpecId())
							.setSql("stock = stock + " + item.getSeckillStock())
							.update();
				}
				productService.lambdaUpdate()
						.eq(Product::getId, item.getProductId())
						.setSql("stock = stock + " + item.getSeckillStock())
						.update();
			}

			// 归还完毕，将活动状态改为禁用，防止重复归还
			seckillActivityMapper.update(null,
					new LambdaUpdateWrapper<SeckillActivity>()
							.eq(SeckillActivity::getId, activity.getId())
							.set(SeckillActivity::getStatus, 2)
			);
		}
	}
}




