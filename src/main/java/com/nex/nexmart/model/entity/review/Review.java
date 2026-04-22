package com.nex.nexmart.model.entity.review;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 商品评价
 * @TableName review
 */
@TableName(value ="review")
@Data
public class Review implements Serializable {
	@TableId(type = IdType.AUTO)
	private Long id;

	/** 评价人，逻辑关联 user.id */
	private Long userId;

	/** 订单项，逻辑关联 order_item.id */
	private Long orderItemId;

	/** 商品，逻辑关联 product.id */
	private Long productId;

	/** 评分 1-5 */
	private Integer rating;

	/** 评价内容 */
	private String content;

	/** 图片/视频链接数组 (JSON 字符串) */
	private String mediaUrls;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;
}
