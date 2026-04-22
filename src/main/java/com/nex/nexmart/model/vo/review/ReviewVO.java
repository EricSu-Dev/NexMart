package com.nex.nexmart.model.vo.review;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewVO {
	private Long id;
	private Long userId;
	private String username;
	private String avatarUrl;
	private Long orderItemId;
	private Long productId;
	private Integer rating;
	private String content;
	private List<String> mediaUrls;
	private LocalDateTime createdAt;
	private Long likeCount;
	private Long commentCount;
	private Boolean liked;
}
