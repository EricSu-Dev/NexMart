package com.nex.nexmart.model.entity.cs;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName cs_message
 */
@TableName(value ="cs_message")
@Data
public class CsMessage implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属会话
     */
    private Long sessionId;

    /**
     * 1用户 2管理员
     */
    private Integer senderType;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 0未读 1已读
     */
    private Integer isRead;

    /**
     * 
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

	private Integer type;//1文字 2图片 3商品 4订单

	private String images;

	private Long productId;

	private Long orderId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}