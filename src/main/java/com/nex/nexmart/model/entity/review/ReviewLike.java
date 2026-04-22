package com.nex.nexmart.model.entity.review;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 评价点赞
 * @TableName review_like
 */
@TableName(value ="review_like")
@Data
public class ReviewLike implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 被点赞的评价，逻辑关联 review.id
     */
    private Long reviewId;

    /**
     * 点赞人，逻辑关联 user.id
     */
    private Long userId;

    /**
     * 
     */
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}