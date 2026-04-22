package com.nex.nexmart.model.entity.checkinPoint;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户签到记录表
 * @TableName user_checkin
 */
@TableName(value ="user_checkin")
@Data
public class UserCheckin implements Serializable {
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
     * 签到日期
     */
    private LocalDate checkinDate;

    /**
     * 本次获得积分
     */
    private Integer pointsEarned;

    /**
     * 签到时的连续天数快照
     */
    private Integer consecutiveDays;

    /**
     * 
     */
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}