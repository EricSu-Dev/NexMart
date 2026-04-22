package com.nex.nexmart.model.vo.review;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewCommentVO {
	private Long id;
	private Long reviewId;
	private Long userId;
	private String username;
	private String avatarUrl;
	private Long parentId;
	private String content;
	private LocalDateTime createdAt;
	private List<ReviewCommentVO> replies;
}
