package com.nex.nexmart.model.dto.seckill;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeckillActivityDTO {
	@NotBlank(message = "活动名称不能为空")
	private String name;
	private String description;
	@NotNull(message = "开始时间不能为空")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startTime;
	@NotNull(message = "结束时间不能为空")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endTime;
	@NotNull(message = "活动类型不能为空")
	private Integer activityType;
}
