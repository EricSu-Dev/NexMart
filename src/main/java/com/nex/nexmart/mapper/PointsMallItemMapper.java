package com.nex.nexmart.mapper;

import com.nex.nexmart.model.entity.checkinPoint.PointsMallItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nex.nexmart.model.vo.checkinPoint.PointsMallItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Eric
*  针对表【points_mall_item(积分商城兑换项表)】的数据库操作Mapper
*  2026-04-12 13:44:58
*  com.nex.nexmart.model.entity.checkinPoint.PointsMallItem
*/
public interface PointsMallItemMapper extends BaseMapper<PointsMallItem> {
	List<PointsMallItemVO> selectItemsWithCoupon(@Param("keyword") String keyword,
	                                              @Param("discountType") Integer discountType,
	                                              @Param("status") Integer status);
}




