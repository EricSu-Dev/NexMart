package com.nex.nexmart.service.intf;

import com.nex.nexmart.model.dto.PointsMallItemDTO;
import com.nex.nexmart.model.entity.checkinPoint.PointsMallItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.vo.ExchangeResultVO;
import com.nex.nexmart.model.vo.checkinPoint.PointsMallItemVO;
import com.nex.nexmart.model.vo.checkinPoint.PointsMallVO;

import java.util.List;

/**
* @author Eric
*  针对表【points_mall_item(积分商城兑换项表)】的数据库操作Service
*  2026-04-12 13:44:58
*/
public interface PointsMallItemService extends IService<PointsMallItem> {
	void createItem(PointsMallItemDTO dto);
	void updateStatus(Long id, Integer status);
	List<PointsMallItemVO> listItems(String keyword, Integer discountType, Integer status);
	void updatePointsCost(Long id, Integer pointsCost);
	void deleteItem(Long id);

	PointsMallVO getUserMall(Long userId);
	ExchangeResultVO exchange(Long userId, Long itemId);
}
