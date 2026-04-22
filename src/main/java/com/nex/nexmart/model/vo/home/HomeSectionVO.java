package com.nex.nexmart.model.vo.home;

import com.nex.nexmart.model.vo.product.ProductVO;
import lombok.Data;

import java.util.List;

@Data
public class HomeSectionVO {
	private Long id;
	private Integer sectionType;
	private Integer autoMode;
	private Integer autoLimit;
	private Integer status;
	private List<ProductVO> items; // 手动模式下已配置的商品列表
}
