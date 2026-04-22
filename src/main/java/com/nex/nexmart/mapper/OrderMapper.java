package com.nex.nexmart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nex.nexmart.model.entity.order.Order;
import com.nex.nexmart.model.vo.dashboard.DailyOrderStatsVO;
import com.nex.nexmart.model.vo.dashboard.DailyRevenueStatsVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Eric
* @description 针对表【order(订单表)】的数据库操作Mapper
* @createDate 2026-03-26 12:43:19
* @Entity com.nex.nexmart.model.entity.order.Order
*/
public interface OrderMapper extends BaseMapper<Order> {
	List<DailyOrderStatsVO> getDailyOrderStats(@Param("days") int days);

	List<DailyRevenueStatsVO> getDailyRevenueStats(@Param("days") int days);

	long countByUserAndSeckillItem(@Param("userId") Long userId, @Param("seckillItemId") Long seckillItemId);
}




