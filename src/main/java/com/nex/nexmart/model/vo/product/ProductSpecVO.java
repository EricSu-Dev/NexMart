package com.nex.nexmart.model.vo.product;
import lombok.Data;

@Data
public class ProductSpecVO {
	private Long id;
	private String specName;
	private Integer sort;
	private  Integer stock;
}
