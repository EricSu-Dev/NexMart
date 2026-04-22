package com.nex.nexmart.model.vo.checkinPoint;

import lombok.Data;

import java.util.List;

@Data
public class PointsMallVO {
	private Integer totalPoints;           // 用户当前积分
	private List<PointsMallItemVO> items;  // 复用管理端已有的 PointsMallItemVO
}
