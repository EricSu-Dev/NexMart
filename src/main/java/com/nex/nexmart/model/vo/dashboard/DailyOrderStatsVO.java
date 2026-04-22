package com.nex.nexmart.model.vo.dashboard;

import lombok.Data;

@Data
public class DailyOrderStatsVO {
	private String date;
	private Long orderCount;   // 当天订单数
}
