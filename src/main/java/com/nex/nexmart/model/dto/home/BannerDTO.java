package com.nex.nexmart.model.dto.home;

import lombok.Data;

@Data
public class BannerDTO {
	private String title;
	private String imageUrl;
	private Long productId;
	private Integer sort;
	private Integer status;
}
