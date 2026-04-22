package com.nex.nexmart.common;

import lombok.Data;

@Data
public class IntentResult {
	private String intent;  // query_product / query_seckill / query_order / query_coupon / query_points / query_promotion / general
	private String keyword; // 查询关键词，general时为null
}
