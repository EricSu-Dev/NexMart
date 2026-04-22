package com.nex.nexmart.mapper.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nex.nexmart.model.entity.order.Payment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
