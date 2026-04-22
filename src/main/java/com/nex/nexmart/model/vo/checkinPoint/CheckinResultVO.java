package com.nex.nexmart.model.vo.checkinPoint;

import lombok.Data;

@Data
public class CheckinResultVO {
	private Integer pointsEarned;
	private Integer consecutiveDays;
	private Integer totalPoints;
	private String bonusRemark;
}