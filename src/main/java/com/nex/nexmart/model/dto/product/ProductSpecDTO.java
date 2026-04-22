package com.nex.nexmart.model.dto.product;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductSpecDTO {
	//productId 不应该放在 ProductSpecDTO 里，原因是：
	//DTO 是接收前端传来的数据，前端创建商品时 productId 还不存在，是后端保存商品后才生成的
	@NotBlank(message = "规格名称不能为空")
	private String specName;
	private Integer sort=0;
	private  Integer stock;

}
