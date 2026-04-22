package com.nex.nexmart.service.intf.checkinPoint;

import com.nex.nexmart.model.entity.checkinPoint.UserCheckin;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.model.vo.checkinPoint.CheckinResultVO;
import com.nex.nexmart.model.vo.checkinPoint.CheckinStatusVO;

import java.time.LocalDate;

/**
* @author Eric
*  针对表【user_checkin(用户签到记录表)】的数据库操作Service
*  2026-04-11 18:06:08
*/
public interface UserCheckinService extends IService<UserCheckin> {
	CheckinResultVO checkin(Long userId);
	CheckinStatusVO getStatus(Long userId, LocalDate target);
}
