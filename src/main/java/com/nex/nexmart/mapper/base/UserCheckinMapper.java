package com.nex.nexmart.mapper.base;

import com.nex.nexmart.model.entity.checkinPoint.UserCheckin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Eric
* 针对表【user_checkin(用户签到记录表)】的数据库操作Mapper
*  2026-04-11 18:06:08
*  com.nex.nexmart.model.entity.checkinPoint.UserCheckin
*/
public interface UserCheckinMapper extends BaseMapper<UserCheckin> {
	// 查询某用户某月已签到的日期列表
	List<Integer> selectCheckedDaysOfMonth(@Param("userId") Long userId,
	                                       @Param("year") int year,
	                                       @Param("month") int month);
}




