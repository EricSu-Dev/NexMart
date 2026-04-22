package com.nex.nexmart.mapper;

import com.nex.nexmart.model.entity.checkinPoint.UserPoints;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
* @author Eric
*  针对表【user_points(用户积分账户表)】的数据库操作Mapper
*  2026-04-11 18:06:52
*  com.nex.nexmart.model.entity.checkinPoint.UserPoints
*/
public interface UserPointsMapper extends BaseMapper<UserPoints> {
	//MyBatis-Plus 有 saveOrUpdate()，但它是先查再写的两步操作，在并发场景下不是原子的，积分这种数据不能用。
	//ON DUPLICATE KEY UPDATE 是数据库层面的原子操作，只有手写 SQL 才能实现，所以用注解。
	@Update("INSERT INTO user_points (user_id, total_points) VALUES (#{userId}, #{delta}) " +
			"ON DUPLICATE KEY UPDATE total_points = total_points + #{delta}")
	void upsertPoints(@Param("userId") Long userId, @Param("delta") int delta);

	@Update("UPDATE user_points SET total_points = total_points - #{delta} " +
			"WHERE user_id = #{userId} AND total_points >= #{delta}")
	int deductPoints(@Param("userId") Long userId, @Param("delta") int delta);
}




