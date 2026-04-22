package com.nex.nexmart.model.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 商品浏览记录表
 * @TableName product_browse_history
 */
@TableName(value ="product_browse_history")
@Data
public class ProductBrowseHistory implements Serializable {
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
     * 
     */
    private Long productId;

    /**
     * 
     */
    private LocalDateTime viewedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}