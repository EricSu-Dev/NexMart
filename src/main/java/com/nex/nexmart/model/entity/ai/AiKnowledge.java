package com.nex.nexmart.model.entity.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI客服知识库
 * @TableName ai_knowledge
 */
@TableName(value = "ai_knowledge")
@Data
public class AiKnowledge implements Serializable {

	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 知识标题
	 */
	private String title;

	/**
	 * 知识分类，如 order/refund/coupon/seckill
	 */
	private String category;

	/**
	 * 知识内容
	 */
	private String content;

	/**
	 * 标签，逗号分隔
	 */
	private String tags;

	/**
	 * 状态：1启用，0禁用
	 */
	private Integer status;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;
}
