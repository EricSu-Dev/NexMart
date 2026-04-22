package com.nex.nexmart.model.entity.checkinPoint;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 签到积分规则表
 * @TableName checkin_points_rule
 */
@TableName(value ="checkin_points_rule")
@Data
public class CheckinPointsRule implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 连续天数节点，0=普通签到
     */
    private Integer consecutiveDays;

    /**
     * 该节点获得积分
     */
    private Integer points;

    /**
     * 
     */
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}