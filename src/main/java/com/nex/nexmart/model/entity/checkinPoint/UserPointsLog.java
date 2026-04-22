package com.nex.nexmart.model.entity.checkinPoint;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 积分流水表
 * @TableName user_points_log
 */
@TableName(value ="user_points_log")
@Data
public class UserPointsLog implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long userId;

    /**
     * 1=签到 2=兑换消费
     */
    private Integer changeType;

    /**
     * 正=增加 负=减少
     */
    private Integer pointsDelta;

    /**
     * 变动后余额快照
     */
    private Integer balance;

    /**
     * 
     */
    private String remark;

    /**
     * 关联业务ID（预留）
     */
    private Long refId;

    /**
     * 
     */
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}