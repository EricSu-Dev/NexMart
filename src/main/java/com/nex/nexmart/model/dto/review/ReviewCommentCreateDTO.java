package com.nex.nexmart.model.dto.review;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewCommentCreateDTO {

	@NotBlank(message = "评论内容不能为空")
	private String content;

	/** 回复的评论ID，NULL 表示一级评论 */
	private Long parentId;
}

