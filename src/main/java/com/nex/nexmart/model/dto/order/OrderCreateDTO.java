package com.nex.nexmart.model.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OrderCreateDTO {

    /** 选中的购物车条目 ID 列表 */
    @NotEmpty(message = "请选择商品")
    private List<@NotNull(message = "购物车条目ID不能为空") Long> cartItemIds;

    /** 关联的地址 ID */
    @NotNull(message = "请选择收货地址")
    private Long addressId;

    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @NotBlank(message = "收货人手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "收货人手机号格式错误")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    private String address;

    @Size(max = 200, message = "备注不能超过200个字符")
    private String remark;

	private Long orderUserCouponId;
	private Map<Long, Long> productCouponMap; // key=cartItemId, value=userCouponId
}
