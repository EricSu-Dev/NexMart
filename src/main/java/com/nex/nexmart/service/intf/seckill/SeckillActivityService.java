package com.nex.nexmart.service.intf.seckill;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.model.dto.seckill.SeckillActivityDTO;
import com.nex.nexmart.model.entity.seckill.SeckillActivity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.vo.seckill.SeckillActivityVO;

import java.util.List;
import java.util.Map;

/**
* @author Eric
*  针对表【seckill_activity(秒杀活动表)】的数据库操作Service
*  2026-04-13 16:10:34
*/
public interface SeckillActivityService extends IService<SeckillActivity> {
	void createActivity(SeckillActivityDTO dto);
	void updateActivity(Long id, SeckillActivityDTO dto);
	void deleteActivity(Long id);
	PageResult<SeckillActivityVO> pageActivity(Integer current, Integer size, Integer status, Integer phase, Integer activityType);
	void updateStatus(Long id, Integer status);
	Map<Long,String> getActivityNameMap(Integer activityType);
	//------------------------活动列表-------------------------------
	List<SeckillActivityVO> listActivity(Integer activityType);
}

