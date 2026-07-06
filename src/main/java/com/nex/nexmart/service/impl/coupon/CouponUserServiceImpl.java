package com.nex.nexmart.service.impl.coupon;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.mapper.base.CartItemMapper;
import com.nex.nexmart.mapper.base.ProductMapper;
import com.nex.nexmart.model.entity.CartItem;
import com.nex.nexmart.model.entity.coupon.Coupon;
import com.nex.nexmart.model.entity.coupon.CouponUser;
import com.nex.nexmart.model.entity.product.Product;
import com.nex.nexmart.model.vo.coupon.AvailableCouponVO;
import com.nex.nexmart.model.vo.coupon.MyCouponVO;
import com.nex.nexmart.service.intf.coupon.CouponService;
import com.nex.nexmart.service.intf.coupon.CouponUserService;
import com.nex.nexmart.mapper.CouponUserMapper;
import com.nex.nexmart.util.CouponDescHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author Eric
*  针对表【coupon_user(用户持有优惠券表)】的数据库操作Service实现
*  2026-04-08 21:10:29
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class CouponUserServiceImpl extends ServiceImpl<CouponUserMapper, CouponUser> implements CouponUserService{
	private  final CouponUserMapper couponUserMapper;
	private  final CouponService couponService;
	private  final CouponDescHelper couponDescHelper;
	private  final CartItemMapper cartItemMapper;
	private  final ProductMapper productMapper;
	private static final int STATUS_UNUSED = 0;
	private static final int STATUS_EXPIRED = 2;
	@Override
	@Transactional
	public void receiveCoupon(Long couponId, Long userId) {
		Coupon coupon = couponService.getById(couponId);
		if (coupon == null) throw new RuntimeException("优惠券不存在");
		if (coupon.getStatus() != 1) throw new RuntimeException("优惠券已下架");

		LocalDateTime now = LocalDateTime.now();
		if (now.isBefore(coupon.getReceiveStart()) || now.isAfter(coupon.getReceiveEnd())) {
			throw new RuntimeException("不在领取时间内");
		}

		// 检查剩余数量
		if (coupon.getTotal() != -1 && coupon.getRemained() <= 0) {
			throw new RuntimeException("优惠券已领完");
		}

		// 检查是否超出每人限领数
		couponService.lambdaUpdate()
				.eq(Coupon::getId, couponId)
				.setSql("updated_at = updated_at")
				.update();

		long myCount = lambdaQuery()
						.eq(CouponUser::getUserId, userId)
						.eq(CouponUser::getCouponId, couponId)
						.count();
		if (myCount >= coupon.getPerLimit()) {
			throw new RuntimeException("已达领取上限");
		}

		// 写入领取记录
		if (coupon.getTotal() != -1) {
			boolean stockUpdated = couponService.lambdaUpdate()
					.eq(Coupon::getId, couponId)
					.gt(Coupon::getRemained, 0)
					.setSql("remained = remained - 1")
					.update();
			if (!stockUpdated) {
				throw new RuntimeException("Coupon stock is not enough");
			}
		}

		CouponUser couponUser = new CouponUser();
		couponUser.setUserId(userId);
		couponUser.setCouponId(couponId);
		couponUser.setCouponType(coupon.getCouponType());
		couponUser.setStatus(0);
		couponUser.setReceivedAt(now);
		couponUser.setExpireAt(now.plusDays(coupon.getValidDays()));
		couponUserMapper.insert(couponUser);

		// 扣减剩余数量
	}

	@Override
	public List<MyCouponVO> getMyCoupons(Long userId, Integer status,Integer couponType) {
		LocalDateTime now = LocalDateTime.now();

		var query = lambdaQuery()
				.eq(CouponUser::getUserId, userId)
				.eq(couponType != null, CouponUser::getCouponType, couponType);

		if (status == null) {
			query.orderByDesc(CouponUser::getReceivedAt);
		} else if (status == STATUS_UNUSED) {
			query.eq(CouponUser::getStatus, STATUS_UNUSED)
					.gt(CouponUser::getExpireAt, now)
					.orderByDesc(CouponUser::getReceivedAt);
		} else if (status == STATUS_EXPIRED) {
			query.and(wrapper -> wrapper
							.eq(CouponUser::getStatus, STATUS_EXPIRED)
							.or()
							.eq(CouponUser::getStatus, STATUS_UNUSED)
							.le(CouponUser::getExpireAt, now))
					.orderByDesc(CouponUser::getReceivedAt);
		} else {
			query.eq(CouponUser::getStatus, status)
					.orderByDesc(CouponUser::getReceivedAt);
		}

		List<CouponUser> myList = query.list();

		if (myList.isEmpty()) return Collections.emptyList();

		// 批量查券
		Set<Long> couponIds = myList.stream()
				.map(CouponUser::getCouponId)
				.collect(Collectors.toSet());
		List<Coupon> coupons = couponService.listByIds(couponIds);

		Map<Long, Coupon> couponMap = coupons.stream()
				.collect(Collectors.toMap(Coupon::getId, c -> c));

		CouponDescHelper.ScopeNameMaps scopeNameMaps = couponDescHelper.buildScopeNameMaps(coupons);

		return myList.stream().map(cu -> {
			Coupon coupon = couponMap.get(cu.getCouponId());
			if (coupon == null) return null;

			MyCouponVO vo = new MyCouponVO();
			BeanUtils.copyProperties(coupon, vo);
			vo.setId(cu.getId());
			vo.setCouponId(cu.getCouponId());
			vo.setExpireAt(cu.getExpireAt());
			vo.setReceivedAt(cu.getReceivedAt());

			// 实时判断过期
			int realStatus = cu.getStatus();
			if (realStatus == STATUS_UNUSED && !cu.getExpireAt().isAfter(now)) {
				realStatus = STATUS_EXPIRED;
			}
			vo.setStatus(realStatus);

			couponDescHelper.fillCommonDesc(coupon,
					vo::setDiscountTypeDesc,
					vo::setDiscountTypeDesc,
					vo::setScopeDesc,
					vo::setScopeName,
					scopeNameMaps.getCategoryNameMap(),
					scopeNameMaps.getProductNameMap());
			return vo;
		}).filter(Objects::nonNull)
		.collect(Collectors.toList());
	}

	@Override
	public List<AvailableCouponVO> getAvailableProductCoupons(Long userId, Long cartItemId) {
		LocalDateTime now = LocalDateTime.now();

		// 查用户未使用未过期的券
		List<CouponUser> myList = lambdaQuery()
						.eq(CouponUser::getUserId, userId)
						.eq(CouponUser::getCouponType, 1)
						.eq(CouponUser::getStatus, 0)
						.gt(CouponUser::getExpireAt, now)
						.list();
		if (myList.isEmpty()) return Collections.emptyList();

		// 批量查券(重复的券去掉)
		Set<Long> couponIds = myList.stream()
				.map(CouponUser::getCouponId)
				.collect(Collectors.toSet());
		List<Coupon> coupons = couponService.listByIds(couponIds);
		Map<Long, Coupon> couponMap = coupons.stream()
				.collect(Collectors.toMap(Coupon::getId, c -> c));

		CouponDescHelper.ScopeNameMaps scopeNameMaps = couponDescHelper.buildScopeNameMaps(coupons);

		CartItem cartItem = cartItemMapper.selectById(cartItemId);
		Product product = productMapper.selectById(cartItem.getProductId());
		return myList.stream().map(cu -> {
					Coupon coupon = couponMap.get(cu.getCouponId());
					if (coupon == null) return null;

					AvailableCouponVO vo = new AvailableCouponVO();
					BeanUtils.copyProperties(coupon, vo);
					vo.setUserCouponId(cu.getId());
					vo.setExpireAt(cu.getExpireAt());

					// 填充范围描述
					couponDescHelper.fillCommonDesc(
							coupon,
							vo::setCouponTypeDesc,
							vo::setDiscountTypeDesc,
							vo::setScopeDesc,
							vo::setScopeName,
							scopeNameMaps.getCategoryNameMap(),
							scopeNameMaps.getProductNameMap());
					// 判断是否可用
					vo.setUsable(isCouponUsable(coupon, product));

					return vo;
				}).filter(Objects::nonNull)
				.sorted(Comparator.comparing(AvailableCouponVO::getUsable).reversed()) // 可用的排前面
				.toList();
	}


	@Override
	public List<AvailableCouponVO> getAvailableOrderCoupons(Long userId, List<Long> cartItemIds) {
		LocalDateTime now = LocalDateTime.now();

		// 查用户未使用未过期的券
		List<CouponUser> myList = lambdaQuery()
				.eq(CouponUser::getUserId, userId)
				.eq(CouponUser::getCouponType, 2)
				.eq(CouponUser::getStatus, 0)
				.gt(CouponUser::getExpireAt, now)
				.list();
		if (myList.isEmpty()) return Collections.emptyList();

		// 批量查券(重复的券去掉)
		Set<Long> couponIds = myList.stream()
				.map(CouponUser::getCouponId)
				.collect(Collectors.toSet());
		List<Coupon> coupons = couponService.listByIds(couponIds);
		Map<Long, Coupon> couponMap = coupons.stream()
				.collect(Collectors.toMap(Coupon::getId, c -> c));

		CouponDescHelper.ScopeNameMaps scopeNameMaps = couponDescHelper.buildScopeNameMaps(coupons);

		return myList.stream().map(cu -> {
					Coupon coupon = couponMap.get(cu.getCouponId());
					if (coupon == null) return null;

					AvailableCouponVO vo = new AvailableCouponVO();
					BeanUtils.copyProperties(coupon, vo);
					vo.setUserCouponId(cu.getId());
					vo.setExpireAt(cu.getExpireAt());

					// 填充范围描述
					couponDescHelper.fillCommonDesc(
							coupon,
							vo::setCouponTypeDesc,
							vo::setDiscountTypeDesc,
							vo::setScopeDesc,
							vo::setScopeName,
							scopeNameMaps.getCategoryNameMap(),
							scopeNameMaps.getProductNameMap());

					// 判断是否可用
					if(coupon.getDiscountType() == 2 || coupon.getDiscountType() == 3) {
						vo.setUsable(true);
					}

					return vo;
				}).filter(Objects::nonNull)
				.sorted(Comparator.comparing(AvailableCouponVO::getUsable).reversed()) // 可用的排前面
				.toList();
	}

	private boolean isCouponUsable(Coupon coupon, Product product) {
		// 判断折扣类型是否满足条件（抽取公共逻辑）
		boolean discountTypeUsable = coupon.getDiscountType() == 2
				|| coupon.getDiscountType() == 3
				|| (coupon.getDiscountType() == 1
				&& product.getPrice().compareTo(coupon.getMinAmount()) >= 0);

		return switch (coupon.getScope()) {
			case 1 -> discountTypeUsable;
			case 2 -> product.getCategoryId().equals(coupon.getScopeId()) && discountTypeUsable;
			case 3 -> product.getId().equals(coupon.getScopeId()) && discountTypeUsable;
			default -> false;
		};
	}

	@Scheduled(fixedDelay = 60000)
	public void markExpiredCoupons() {
		LocalDateTime now = LocalDateTime.now();
		int updated = couponUserMapper.update(
				null,
				new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<CouponUser>()
						.eq(CouponUser::getStatus, STATUS_UNUSED)
						.le(CouponUser::getExpireAt, now)
						.set(CouponUser::getStatus, STATUS_EXPIRED)
		);
		if (updated > 0) {
			log.info("优惠券过期状态更新完成: {} 张", updated);
		}
	}

}




