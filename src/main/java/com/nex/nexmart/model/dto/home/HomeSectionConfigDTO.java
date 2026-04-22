package com.nex.nexmart.model.dto.home;

import lombok.Data;

@Data
public class HomeSectionConfigDTO {
	private Integer autoMode;   // 0手动 1自动
	private Integer autoLimit;  // 自动模式取前N条
	private Integer status;     // 0禁用 1启用
}
