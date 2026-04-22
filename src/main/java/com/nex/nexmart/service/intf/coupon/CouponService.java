package com.nex.nexmart.service.intf.coupon;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.model.dto.coupon.CouponCreateDTO;
import com.nex.nexmart.model.dto.coupon.CouponPageQueryDTO;
import com.nex.nexmart.model.dto.coupon.CouponUpdateDTO;
import com.nex.nexmart.model.entity.coupon.Coupon;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.vo.coupon.CouponListVO;
import com.nex.nexmart.model.vo.coupon.CouponStatsVO;
import com.nex.nexmart.model.vo.coupon.CouponVO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
* @author Eric
*  针对表【coupon(优惠券模板表)】的数据库操作Service
*  2026-04-08 21:08:53
*/
public interface CouponService extends IService<Coupon> {
	void createCoupon(CouponCreateDTO dto);
	PageResult<CouponVO> pageCoupon(CouponPageQueryDTO dto);
	void updateCoupon(CouponUpdateDTO dto);
	void updateStatus(Long id, Integer status);
	void deleteCoupon(Long id);
	CouponStatsVO getCouponStats(Long id);

	List<CouponListVO> getAvailableCoupons(Long userId,Integer discountType);

}
