package com.nex.nexmart.service.impl.review;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.OrderMapper;
import com.nex.nexmart.mapper.base.ReviewMapper;
import com.nex.nexmart.model.dto.review.ReviewCommentCreateDTO;
import com.nex.nexmart.model.dto.review.ReviewCreateDTO;
import com.nex.nexmart.model.entity.*;
import com.nex.nexmart.model.entity.order.Order;
import com.nex.nexmart.model.entity.order.OrderItem;
import com.nex.nexmart.model.entity.review.Review;
import com.nex.nexmart.model.entity.review.ReviewComment;
import com.nex.nexmart.model.entity.review.ReviewLike;
import com.nex.nexmart.model.vo.review.ReviewCommentVO;
import com.nex.nexmart.model.vo.review.ReviewLikeVO;
import com.nex.nexmart.model.vo.review.ReviewVO;
import com.nex.nexmart.service.intf.*;
import com.nex.nexmart.service.intf.order.OrderItemService;
import com.nex.nexmart.service.intf.review.ReviewCommentService;
import com.nex.nexmart.service.intf.review.ReviewLikeService;
import com.nex.nexmart.service.intf.review.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

	private final ReviewLikeService reviewLikeService;
	private final ReviewCommentService reviewCommentService;
	private final OrderItemService orderItemService;
	private final OrderMapper orderMapper;
	private final UserService userService;
	private final ObjectMapper objectMapper;

	@Override
	public void createReview(@Valid ReviewCreateDTO dto, Long userId) {
		OrderItem orderItem = orderItemService.getById(dto.getOrderItemId());
		if (orderItem == null) {
			throw new BusinessException("订单项不存在");
		}
		if (!Objects.equals(orderItem.getProductId(), dto.getProductId())) {
			throw new BusinessException("商品信息不匹配");
		}
		Order order = orderMapper.selectById(orderItem.getOrderId());
		if (order == null || !order.getUserId().equals(userId)) {
			throw new BusinessException("订单不存在");
		}
		if (order.getStatus() != 4) {
			throw new BusinessException("订单未完成，无法评价");
		}
		if (lambdaQuery().eq(Review::getOrderItemId, dto.getOrderItemId()).count() > 0) {
			throw new BusinessException("该订单项已评价");
		}

		Review review = new Review();
		BeanUtils.copyProperties(dto, review);
		review.setUserId(userId);
		review.setContent(dto.getContent().trim());

		if (dto.getMediaUrls() != null && !dto.getMediaUrls().isEmpty()) {
			try {
				//把 Java 的 List<String> 集合转换成 JSON 字符串存入数据库
				review.setMediaUrls(objectMapper.writeValueAsString(dto.getMediaUrls()));
			} catch (Exception e) {
				throw new BusinessException("媒体链接格式错误");
			}
		}

		save(review);
	}

	@Override
	public PageResult<ReviewVO> pageByProduct(Long productId, long current, long size, Long userId) {
		Page<Review> page = lambdaQuery()
				.eq(Review::getProductId, productId)
				.orderByDesc(Review::getCreatedAt)
				.page(new Page<>(current, size));

		List<Review> reviews = page.getRecords();
		if (reviews.isEmpty()) {
			//// page.convert()，保留原有分页信息,如total=0, size=10, current=1, pages=0
			/*
			原始 page 对象：
			├── records  → [Review{}, Review{}...]  ← 这部分被转换
			├── total    → 0
			├── size     → 10                       ← 这些分页信息原样保留
			├── current  → 1
			└── pages    → 0

			转换后 emptyVoPage：
			├── records  → [ReviewVO{}, ReviewVO{}...]  ← 转换成新类型
			├── total    → 0
			├── size     → 10                            ← 分页信息照样保留
			├── current  → 1
			└── pages    → 0
			 */
			/*  IPage<ReviewVO> emptyVoPage = page.convert(r -> new ReviewVO());
			//转换后：[ReviewVO{}, ReviewVO{}, ...],setRecords 后：[]
			emptyVoPage.setRecords(List.of());*/

			Page<ReviewVO> emptyVoPage = new Page<>(page.getCurrent(), page.getSize(), 0);
			emptyVoPage.setRecords(List.of());
			PageResult<ReviewVO> empty = PageResult.of(emptyVoPage);
			empty.setAvgRating(0.0);
			return empty;
		}

		List<Long> reviewIds = reviews.stream().map(Review::getId).collect(Collectors.toList());
		Set<Long> userIds = reviews.stream().map(Review::getUserId).collect(Collectors.toSet());
		//获取用户信息,用于获取用户的头像和昵称
		Map<Long, User> userMap = userService.lambdaQuery()
				.in(User::getId, userIds)
				.list()
				.stream()
				.collect(Collectors.toMap(User::getId, u -> u));
		//把点赞记录列表转换成每条评价对应点赞数量的 Map
		Map<Long, Long> likeCountMap = reviewLikeService.lambdaQuery()
				.in(ReviewLike::getReviewId, reviewIds)
				.list()
				.stream()
				.collect(Collectors.groupingBy(ReviewLike::getReviewId, Collectors.counting()));
		//把评论记录列表转换成每条评价对应评论数量的 Map
		Map<Long, Long> commentCountMap = reviewCommentService.lambdaQuery()
				.in(ReviewComment::getReviewId, reviewIds)
				.list()
				.stream()
				.collect(Collectors.groupingBy(ReviewComment::getReviewId, Collectors.counting()));
		//创建一个空的不可变集合 Set
		Set<Long> likedReviewIds = Collections.emptySet();
		//查出当前用户点赞过的评论ID集合
		if (userId != null) {
			likedReviewIds = reviewLikeService.lambdaQuery()
					.eq(ReviewLike::getUserId, userId)
					.in(ReviewLike::getReviewId, reviewIds)
					.list()
					.stream()
					.map(ReviewLike::getReviewId)
					.collect(Collectors.toSet());
		}
		//加final定义，不修改likedReviewIds
		final Set<Long> finalLikedIds = likedReviewIds;

		// ✅ 用 page.convert() 直接转换，保留分页信息
		IPage<ReviewVO> voPage = page.convert(r -> {
			ReviewVO vo = new ReviewVO();
			BeanUtils.copyProperties(r, vo);
			//该评价的点赞数量
			vo.setLikeCount(likeCountMap.getOrDefault(r.getId(), 0L));
			//该评价的评论数量
			vo.setCommentCount(commentCountMap.getOrDefault(r.getId(), 0L));
			//该评价当前用户是否点赞
			vo.setLiked(finalLikedIds.contains(r.getId()));

			User u = userMap.get(r.getUserId());
			if (u != null) {
				//获取发布者的昵称和头像
				vo.setUsername(u.getUsername());
				vo.setAvatarUrl(u.getAvatarUrl());
			}

			if (StringUtils.hasText(r.getMediaUrls())) {
				try {
					//把数据库中存的 JSON 字符串转换成 Java 的 List 集合
					vo.setMediaUrls(objectMapper.readValue(r.getMediaUrls(), new TypeReference<List<String>>() {
					}));
				} catch (Exception e) {
					vo.setMediaUrls(List.of());
				}
			} else {
				vo.setMediaUrls(List.of());
			}
			return vo;
		});

		// ✅ 类型匹配，不报错
		PageResult<ReviewVO> result = PageResult.of(voPage);
		result.setAvgRating(getAvgRating(productId));
		return result;
	}

	@Override
	public ReviewVO getReviewDetail(Long reviewId, Long userId) {
		Review review = getById(reviewId);
		if (review == null) {
			throw new BusinessException("评价不存在");
		}
		ReviewVO vo = new ReviewVO();
		BeanUtils.copyProperties(review, vo);
		User u = userService.getById(review.getUserId());
		if (u != null) {
			vo.setUsername(u.getUsername());
			vo.setAvatarUrl(u.getAvatarUrl());
		}

		vo.setLikeCount(reviewLikeService.lambdaQuery().eq(ReviewLike::getReviewId, reviewId).count());
		vo.setCommentCount(reviewCommentService.lambdaQuery().eq(ReviewComment::getReviewId, reviewId).count());

		if (userId != null) {
			boolean liked = reviewLikeService.lambdaQuery()
					.eq(ReviewLike::getReviewId, reviewId)
					.eq(ReviewLike::getUserId, userId)
					.count() > 0;
			vo.setLiked(liked);
		} else {
			vo.setLiked(false);
		}

		if (StringUtils.hasText(review.getMediaUrls())) {
			try {
				vo.setMediaUrls(objectMapper.readValue(review.getMediaUrls(), new TypeReference<List<String>>() {
				}));
			} catch (Exception e) {
				vo.setMediaUrls(List.of());
			}
		} else {
			vo.setMediaUrls(List.of());
		}
		return vo;
	}

	//点赞
	@Override
	public ReviewLikeVO toggleLike(Long reviewId, Long userId) {
		Review review = getById(reviewId);
		if (review == null) {
			throw new BusinessException("评价不存在");
		}

		ReviewLike existing = reviewLikeService.lambdaQuery()
				.eq(ReviewLike::getReviewId, reviewId)
				.eq(ReviewLike::getUserId, userId)
				.one();

		boolean liked;
		//如果点赞则取消点赞
		if (existing != null) {
			reviewLikeService.removeById(existing.getId());
			liked = false;
		} else {
			//否则直接点赞
			ReviewLike like = new ReviewLike();
			like.setReviewId(reviewId);
			like.setUserId(userId);
			reviewLikeService.save(like);
			liked = true;
		}
		//返回点赞/取消点赞后的点赞总数
		long likeCount = reviewLikeService.lambdaQuery().eq(ReviewLike::getReviewId, reviewId).count();
		ReviewLikeVO vo = new ReviewLikeVO();
		vo.setLikeCount(likeCount);
		vo.setLiked(liked);
		return vo;
	}

	@Override
	public List<ReviewCommentVO> listComments(Long reviewId) {
		List<ReviewComment> comments = reviewCommentService.lambdaQuery()
				.eq(ReviewComment::getReviewId, reviewId)
				.orderByAsc(ReviewComment::getCreatedAt)
				.list();

		if (comments.isEmpty()) {
			return List.of();
		}
		//获取用户的map集合来获取对应用户的昵称和头像
		Set<Long> userIds = comments.stream().map(ReviewComment::getUserId).collect(Collectors.toSet());
		Map<Long, User> userMap = userService.lambdaQuery()
				.in(User::getId, userIds)
				.list()
				.stream()
				.collect(Collectors.toMap(User::getId, u -> u));
		//// 作用：用评论ID快速找到对应的VO对象
		Map<Long, ReviewCommentVO> idToVo = new HashMap<>();
		//// 作用：只存根评论（parentId=null的）
		List<ReviewCommentVO> roots = new ArrayList<>();

		for (ReviewComment c : comments) {
			ReviewCommentVO vo = new ReviewCommentVO();
			BeanUtils.copyProperties(c, vo);
			//创建一个空的子评论列表
			vo.setReplies(new ArrayList<>());

			User u = userMap.get(c.getUserId());
			if (u != null) {
				vo.setUsername(u.getUsername());
				vo.setAvatarUrl(u.getAvatarUrl());
			}
			// 存入Map，方便后续通过ID查找
			idToVo.put(c.getId(), vo);
			// 没有父评论 → 是根评论 → 加入roots
			if (c.getParentId() == null) {
				roots.add(vo);
			}
		}

		for (ReviewComment c : comments) {
			//如果是子评论,把子评论添加到父评论的子评论列表中
			if (c.getParentId() != null) {
				ReviewCommentVO parent = idToVo.get(c.getParentId());
				ReviewCommentVO child = idToVo.get(c.getId());
				if (parent != null && child != null) {
					parent.getReplies().add(child);
				}
			}
		}

		return roots;
	}

	@Override
	public void addComment(Long reviewId, ReviewCommentCreateDTO dto, Long userId) {
		Review review = getById(reviewId);
		if (review == null) {
			throw new BusinessException("评价不存在");
		}
		Long parentId = dto.getParentId();
		if (parentId != null) {
			ReviewComment parent = reviewCommentService.getById(parentId);
			if (parent == null || !Objects.equals(parent.getReviewId(), reviewId)) {
				throw new BusinessException("回复的评论不存在");
			}
		}

		ReviewComment comment = new ReviewComment();
		comment.setReviewId(reviewId);
		comment.setUserId(userId);
		comment.setParentId(parentId);
		comment.setContent(dto.getContent().trim());
		reviewCommentService.save(comment);
	}

	private double getAvgRating(Long productId) {
		QueryWrapper<Review> wrapper = new QueryWrapper<>();
		wrapper.eq("product_id", productId)
				.select("AVG(rating) AS avgRating");//用数据库的聚合查询
		//Map{ "avgRating" → 4.2 }
		Map<String, Object> result = getMap(wrapper);
		if (result == null) {
			return 0.0;
		}
		Object val = result.get("avgRating");
		if (val instanceof Number) {
			return ((Number) val).doubleValue();
		}
		try {
			return Double.parseDouble(String.valueOf(val));
		} catch (Exception e) {
			return 0.0;
		}
	}


	//    问题                           原代码                           优化后
	//  null检查顺序               !isEmpty() && != null               != null && !isEmpty()
	//  删除评价时过滤了userId         只删当前用户的评论                  删除该评价下所有评论
	//  循环内逐条查询删除           N 次查询 + N 次删除                  1 次批量查询 + 1 次批量删除
	//  删除顺序                    子评论和父评论混合删                  先删子评论，再删父评论

	@Override
	public void deleteReview(Long reviewId, Long userId) {
		Review one = lambdaQuery().eq(Review::getId, reviewId)
				.eq(Review::getUserId, userId)
				.one();
		if (one == null) {
			throw new BusinessException("评价不存在");
		}

		//查询该评价的所有评论,不限任何人
		List<ReviewComment> list = reviewCommentService.lambdaQuery()
				.eq(ReviewComment::getReviewId, reviewId)
				.list();
		//如果有评论,则删除
		if (list != null && !list.isEmpty()) {
			List<Long> commentIdList = list.stream().map(ReviewComment::getId).collect(Collectors.toList());
			//查询所有子评论
			List<ReviewComment> child = reviewCommentService.lambdaQuery().in(ReviewComment::getId, commentIdList).list();
			//删除所有子评论
			if (child != null && !child.isEmpty()) {
				List<Long> childList = child.stream().map(ReviewComment::getId).collect(Collectors.toList());
				reviewCommentService.removeByIds(childList);
			}
			//删除所有评论
			reviewCommentService.removeByIds(commentIdList);
		}
		//最后删除评价
		removeById(one);
	}

	@Override
	public void deleteComment(Long reviewId, Long commentId, Long userId) {
		ReviewComment one = reviewCommentService.lambdaQuery()
				.eq(ReviewComment::getReviewId, reviewId)
				.eq(ReviewComment::getUserId, userId)
				.eq(ReviewComment::getId, commentId)
				.one();
		if (one == null) {
			throw new BusinessException("评论不存在");
		}
		//看有没有子评论
		List<ReviewComment> list = reviewCommentService.lambdaQuery()
				.eq(ReviewComment::getParentId, commentId).list();
		if (list != null && !list.isEmpty()) {
			List<Long> commentIdList = list.stream().map(ReviewComment::getId).collect(Collectors.toList());
			reviewCommentService.removeByIds(commentIdList);
			reviewCommentService.removeById(one);
		}
	}
}
