package com.nex.nexmart.model.entity.home;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName home_section
 */
@TableName(value ="home_section")
@Data
public class HomeSection implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模块类型：1热销商品 2新品上市 3为你推荐
     */
    private Integer sectionType;

    /**
     * 0手动 1自动
     */
    private Integer autoMode;

    /**
     * 自动模式取前N条
     */
    private Integer autoLimit;

    /**
     * 0禁用 1启用
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}