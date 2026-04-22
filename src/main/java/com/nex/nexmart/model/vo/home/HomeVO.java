package com.nex.nexmart.model.vo.home;

import com.nex.nexmart.model.vo.product.ProductVO;
import lombok.Data;

import java.util.List;

@Data
public class HomeVO {
	private List<ProductVO> hotSales;        // 热销商品
	private List<ProductVO> newArrivals;     // 新品上市
	private List<ProductVO> recommendations; // 为你推荐
}