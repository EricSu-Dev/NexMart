package com.nex.nexmart.model.vo.checkinPoint;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointsLogVO {
	private Integer changeType;   // 1=签到 2=兑换消费
	private Integer pointsDelta;  // 正=增加 负=减少
	private Integer balance;      // 变动后余额
	private String remark;
	private LocalDateTime createdAt;
}
