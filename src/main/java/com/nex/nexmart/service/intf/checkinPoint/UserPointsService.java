package com.nex.nexmart.service.intf.checkinPoint;

import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.model.entity.checkinPoint.UserPoints;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.vo.checkinPoint.PointsLogVO;

/**
* @author Eric
* @description 针对表【user_points(用户积分账户表)】的数据库操作Service
* @createDate 2026-04-11 18:06:52
*/
public interface UserPointsService extends IService<UserPoints> {
	// 增加积分，返回变动后余额
	int addPoints(Long userId, int delta);
	// 写流水
	void writeLog(Long userId, int changeType, int delta, int balance, String remark, Long refId);

	PageResult<PointsLogVO> getLogs(Long userId, long current, long size);
}
