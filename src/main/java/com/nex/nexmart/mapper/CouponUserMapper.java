package com.nex.nexmart.mapper;

import com.nex.nexmart.model.entity.coupon.CouponUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author Eric
* @description 针对表【coupon_user(用户持有优惠券表)】的数据库操作Mapper
* @createDate 2026-04-08 21:10:29
* @Entity com.nex.nexmart.model.entity.coupon.CouponUser
*/
public interface CouponUserMapper extends BaseMapper<CouponUser> {
	long countByUserAndCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);

	/** 批量查询用户持有的多张券数量，每行两列：coupon_id, cnt */
	List<Map<String, Object>> countByUserAndCoupons(@Param("userId") Long userId, @Param("couponIds") List<Long> couponIds);
}




