package com.nex.nexmart.model.vo.checkinPoint;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PointsMallItemVO {
	private Long id;
	private Integer pointsCost;
	private Integer status;
	private LocalDateTime createdAt;

	// 关联券信息
	private Long couponId;
	private String couponName;
	private Integer couponType;
	private Integer discountType;
	private Integer remained;       // 剩余可领数量
	private LocalDateTime receiveEnd;
	private Integer perLimit;
	private BigDecimal discountAmount;
	private BigDecimal discountRate;
	private Integer validDays;
	private Integer total;
}
