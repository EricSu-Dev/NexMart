package com.nex.nexmart.service.impl.order;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.model.entity.order.OrderItem;
import com.nex.nexmart.service.intf.order.OrderItemService;
import com.nex.nexmart.mapper.base.OrderItemMapper;
import org.springframework.stereotype.Service;

/**
* @author Eric
* @description 针对表【order_item(订单明细表)】的数据库操作Service实现
* @createDate 2026-03-26 12:43:23
*/
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem>
    implements OrderItemService{

}




