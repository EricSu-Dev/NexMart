package com.nex.nexmart.model.entity.review;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 评价评论
 * @TableName review_comment
 */
@TableName(value ="review_comment")
@Data
public class ReviewComment implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属评价，逻辑关联 review.id
     */
    private Long reviewId;

    /**
     * 评论人，逻辑关联 user.id
     */
    private Long userId;

    /**
     * 回复的评论，逻辑关联 review_comment.id，NULL 表示一级评论
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 
     */
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}