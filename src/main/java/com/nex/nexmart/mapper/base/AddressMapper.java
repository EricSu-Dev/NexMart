package com.nex.nexmart.mapper.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nex.nexmart.model.entity.Address;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author nex
 * @description 针对表【address(收货地址表)】的数据库操作Mapper
 * @Entity com.nex.nexmart.model.entity.Address
 */
@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}
