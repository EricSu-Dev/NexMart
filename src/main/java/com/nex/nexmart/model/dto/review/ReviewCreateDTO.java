package com.nex.nexmart.model.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReviewCreateDTO {

	@NotNull(message = "订单项不能为空")
	private Long orderItemId;

	@NotNull(message = "商品不能为空")
	private Long productId;

	@NotNull(message = "评分不能为空")
	@Min(value = 1, message = "评分最低为1")
	@Max(value = 5, message = "评分最高为5")
	private Integer rating;

	@NotBlank(message = "评价内容不能为空")
	private String content;

	/** 图片/视频链接数组（可选） */
	private List<String> mediaUrls;
}
