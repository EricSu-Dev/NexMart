package com.nex.nexmart.model.entity.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 商品规格
 * @TableName product_spec
 */
@TableName(value ="product_spec")
@Data
public class ProductSpec implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 逻辑关联 product.id
     */
    private Long productId;

    /**
     * 规格名称，如：黑色、白色
     */
    private String specName;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 
     */
    private LocalDateTime createdAt;

	private Integer stock;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}