package com.nex.nexmart.model.vo.dashboard;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailyRevenueStatsVO {
	private String date;       // "2024-03-01"
	private BigDecimal revenue; // 当天营收
}
