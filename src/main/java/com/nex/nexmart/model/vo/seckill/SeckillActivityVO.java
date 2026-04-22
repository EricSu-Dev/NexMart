package com.nex.nexmart.model.vo.seckill;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeckillActivityVO {
	private Long id;
	private String name;
	private String description;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private Integer status;
	private LocalDateTime createdAt;
	private Long itemCount;
	private Integer activityType;
	private Integer phase;//1:未开始 2:进行中 3:已结束
}
