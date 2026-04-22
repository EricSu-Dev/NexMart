package com.nex.nexmart.model.entity.checkinPoint;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 积分商城兑换项表
 * @TableName points_mall_item
 */
@TableName(value ="points_mall_item")
@Data
public class PointsMallItem implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联优惠券模板ID
     */
    private Long couponId;

    /**
     * 兑换所需积分
     */
    private Integer pointsCost;


    /**
     * 1=上架 2=下架
     */
    private Integer status;

    /**
     * 
     */
    private LocalDateTime createdAt;

    /**
     * 
     */
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}