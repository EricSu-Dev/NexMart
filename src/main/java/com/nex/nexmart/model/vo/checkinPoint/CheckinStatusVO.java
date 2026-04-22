package com.nex.nexmart.model.vo.checkinPoint;

import lombok.Data;

import java.util.List;

@Data
public class CheckinStatusVO {
	private List<Integer> checkedDays;   // 本月已签到日期，如 [1, 2, 3, 7]
	private Integer consecutiveDays;     // 当前连续签到天数
	private Boolean todayChecked;
	private Integer totalPoints;
}
