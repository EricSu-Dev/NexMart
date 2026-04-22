package com.nex.nexmart.service.intf.order;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.model.dto.order.ReturnApplyDTO;
import com.nex.nexmart.model.entity.order.ReturnOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.vo.order.ReturnOrderDetailVO;
import com.nex.nexmart.model.vo.order.ReturnOrderVO;

import java.math.BigDecimal;

/**
* @author Eric
* @description 针对表【return_order】的数据库操作Service
* @createDate 2026-03-31 15:56:05
*/
public interface ReturnOrderService extends IService<ReturnOrder> {

	PageResult<ReturnOrderVO> returnOrderList(long current, long size, Integer status);

	void audit(Long returnId, Integer status, String rejectReason, BigDecimal actualRefundAmount);

	void refund(Long returnId);

	void apply(Long id, ReturnApplyDTO dto, Long userId);

	void cancelApply(Long id, Long userId);

	ReturnOrderDetailVO detail(Long id, Long userId);

	ReturnOrderDetailVO adminDetail(Long id);
}
