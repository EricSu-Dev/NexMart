package com.nex.nexmart.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExchangeResultVO {
	private Integer pointsUsed;      // 消耗积分
	private Integer remainPoints;    // 兑换后剩余积分
	private String couponName;       // 券名称
	private LocalDateTime expireAt;  // 券有效期
}
