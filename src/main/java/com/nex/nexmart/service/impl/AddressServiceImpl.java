package com.nex.nexmart.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.base.AddressMapper;
import com.nex.nexmart.model.entity.Address;
import com.nex.nexmart.service.intf.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author nex
 * @description 针对表【address(收货地址表)】的数据库操作Service实现
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {


    @Override
    public List<Address> listByUserId(Long userId) {
		return lambdaQuery()
				.eq(Address::getUserId, userId)
				.orderByDesc(Address::getIsDefault)
				.orderByDesc(Address::getUpdatedAt)
				.list();
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long addressId) {
        // 1. 将该用户所有地址设为非默认
        this.update(new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0));

        // 2. 将指定地址设为默认
        this.update(new LambdaUpdateWrapper<Address>()
                .eq(Address::getId, addressId)
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 1));
    }

	@Override
	@Transactional
	public void addAddress(Address address) {
		// 判断是否已有地址，没有则强制设为默认
		boolean hasAddress = lambdaQuery()
				.eq(Address::getUserId, address.getUserId())
				.exists();
		if (!hasAddress) {
			address.setIsDefault(1);
		}

		// 如果新增时指定为默认，先清除其他默认
		if (address.getIsDefault() != null && address.getIsDefault() == 1) {
			lambdaUpdate()
					.eq(Address::getUserId, address.getUserId())
					.set(Address::getIsDefault, 0)
					.update();
		}

		this.save(address);
	}

	@Override
	@Transactional
	public void updateAddress(Address address) {
		if (address.getId() == null) {
			throw new BusinessException("地址ID不能为空");
		}

		Address one = this.getById(address.getId());
		// ✅ 补充用户归属校验
		if (one == null || !one.getUserId().equals(address.getUserId())) {
			throw new BusinessException("地址不存在");
		}

		// ✅ 原来不是默认，本次设为默认，才需要清除其他
		if (address.getIsDefault() != null && address.getIsDefault() == 1
				&& one.getIsDefault() != 1) {
			lambdaUpdate()
					.eq(Address::getUserId, address.getUserId())
					.set(Address::getIsDefault, 0)
					.update();
		}

		// 原来是默认，本次改为非默认，自动转移
		if (one.getIsDefault() == 1 && address.getIsDefault() == 0) {
			Address next = lambdaQuery()
					.eq(Address::getUserId, address.getUserId())
					.ne(Address::getId, address.getId())
					.orderByDesc(Address::getCreatedAt)
					.last("LIMIT 1")
					.one();
			if (next != null) {
				markAsDefault(next.getId(), address.getUserId());
			} else {
				throw new BusinessException("当前为唯一地址，不能取消默认");
			}
		}
		this.updateById(address);
	}

	@Override
	@Transactional
	public void removeAddress(Long userId, Long addressId) {
		Address address = getById(addressId);
		if (address == null || !address.getUserId().equals(userId)) {
			throw new BusinessException("地址不存在");
		}
		removeById(addressId);
		if (address.getIsDefault() == 1) {
			Address next = lambdaQuery()
					.eq(Address::getUserId, userId)
					.orderByDesc(Address::getCreatedAt)
					.last("LIMIT 1")
					.one();
			if (next != null) {
				// ✅ 直接标记，不走清零逻辑（已删除的就是唯一默认）
				markAsDefault(next.getId(), userId);
			}
		}
	}

	// ✅ 私有方法，仅设置默认标记
	private void markAsDefault(Long addressId, Long userId) {
		lambdaUpdate()
				.eq(Address::getId, addressId)
				.eq(Address::getUserId, userId)
				.set(Address::getIsDefault, 1)
				.update();
	}
}
