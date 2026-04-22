package com.nex.nexmart.service.intf.review;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.model.dto.review.ReviewCommentCreateDTO;
import com.nex.nexmart.model.dto.review.ReviewCreateDTO;
import com.nex.nexmart.model.entity.review.Review;
import com.nex.nexmart.model.vo.review.ReviewCommentVO;
import com.nex.nexmart.model.vo.review.ReviewLikeVO;
import com.nex.nexmart.model.vo.review.ReviewVO;

import java.util.List;

public interface ReviewService extends IService<Review> {

	void createReview(ReviewCreateDTO dto, Long userId);

	PageResult<ReviewVO> pageByProduct(Long productId, long current, long size, Long userId);

	ReviewLikeVO toggleLike(Long reviewId, Long userId);

	List<ReviewCommentVO> listComments(Long reviewId);

	void addComment(Long reviewId, ReviewCommentCreateDTO dto, Long userId);

	ReviewVO getReviewDetail(Long reviewId, Long userId);

	void deleteReview(Long reviewId, Long userId);

	void deleteComment(Long reviewId, Long commentId, Long userId);
}
