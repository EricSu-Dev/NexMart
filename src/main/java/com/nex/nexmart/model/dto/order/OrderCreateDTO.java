package com.nex.nexmart.model.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OrderCreateDTO {

    /** 选中的购物车条目 ID 列表 */
    @NotEmpty(message = "请选择商品")
    private List<Long> cartItemIds;

    /** 关联的地址 ID */
    private Long addressId;

    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @NotBlank(message = "收货人手机号不能为空")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    private String address;

    private String remark;

	private Long orderUserCouponId;
	private Map<Long, Long> productCouponMap; // key=cartItemId, value=userCouponId
}
