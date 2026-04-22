package com.nex.nexmart.service.intf.coupon;

import com.nex.nexmart.model.entity.coupon.CouponUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.vo.coupon.AvailableCouponVO;
import com.nex.nexmart.model.vo.coupon.MyCouponVO;

import java.util.List;

/**
* @author Eric
*  针对表【coupon_user(用户持有优惠券表)】的数据库操作Service
*  2026-04-08 21:10:29
*/
public interface CouponUserService extends IService<CouponUser> {
	void receiveCoupon(Long couponId, Long userId);
	List<MyCouponVO> getMyCoupons(Long userId, Integer status,Integer couponType);
	List<AvailableCouponVO> getAvailableProductCoupons(Long userId, Long cartItemId) ;
	List<AvailableCouponVO> getAvailableOrderCoupons(Long userId, List<Long> cartItemIds);
}
