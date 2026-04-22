package com.nex.nexmart.model.vo.home;

import lombok.Data;

@Data
public class BannerVO {
	private Long id;
	private String title;
	private String imageUrl;
	private Long productId;
	private String productName;
	private Integer sort;
	private Integer status;
}
