package com.nex.nexmart.controller.admin;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.vo.dashboard.DailyOrderStatsVO;
import com.nex.nexmart.model.vo.dashboard.DailyRevenueStatsVO;
import com.nex.nexmart.model.vo.dashboard.DashboardStatsVO;
import com.nex.nexmart.service.intf.DashboardStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理端-概览统计接口")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
public class DashboardStatsController {
	private final DashboardStatsService dashboardStatsService;
	@GetMapping("/stats")
	@Operation(summary = "获取仪表盘数据")
	public Result<DashboardStatsVO> getDashboardStats() {
		return Result.success(dashboardStatsService.stats());
	}

	@GetMapping("/order-trend")
	@Operation(summary = "获取下单趋势数据")
	public Result<List<DailyOrderStatsVO>> getOrderTrend(
			@RequestParam(defaultValue = "30") int days) {
		return Result.success(dashboardStatsService.getDailyOrderStats(days));
	}

	@GetMapping("/revenue-trend")
	@Operation(summary = "获取收入趋势数据")
	public Result<List<DailyRevenueStatsVO>> getRevenueTrend(
			@RequestParam(defaultValue = "30") int days) {
		return Result.success(dashboardStatsService.getDailyRevenueStats(days));
	}
}
