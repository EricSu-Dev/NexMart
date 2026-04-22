package com.nex.nexmart.service.intf;

import com.nex.nexmart.model.vo.dashboard.DailyOrderStatsVO;
import com.nex.nexmart.model.vo.dashboard.DailyRevenueStatsVO;
import com.nex.nexmart.model.vo.dashboard.DashboardStatsVO;

import java.util.List;

public interface DashboardStatsService {

	DashboardStatsVO stats();

	List<DailyOrderStatsVO> getDailyOrderStats(int days);

	List<DailyRevenueStatsVO> getDailyRevenueStats(int days);
}
