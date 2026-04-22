package com.nex.nexmart.model.vo;

import com.nex.nexmart.model.vo.product.ProductVO;
import lombok.Data;

import java.util.List;

@Data
public class AiSearchSuggestVO {
	private String message;
	private List<ProductVO> products; // 复用你现有的ProductVO
}
