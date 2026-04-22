package com.nex.nexmart.service.intf;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.entity.Address;
import java.util.List;

/**
 * @author nex
 * @description 针对表【address(收货地址表)】的数据库操作Service
 */
public interface AddressService extends IService<Address> {
    /**
     * 获取用户所有地址
     */
    List<Address> listByUserId(Long userId);

    /**
     * 设置默认地址
     */
    void setDefault(Long userId, Long addressId);

    /**
     * 安全删除地址（校验用户ID）
     */
    void removeAddress(Long userId, Long addressId);

	/**
	 * 添加地址
	 */
	void addAddress(Address address);

	/**
	 * 修改地址
	 */
	void updateAddress(Address address);
}
