package com.nex.nexmart.service.intf;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.model.dto.login.*;
import com.nex.nexmart.model.entity.User;
import com.nex.nexmart.model.vo.LoginVO;
import com.nex.nexmart.model.vo.UserVO;
import jakarta.validation.Valid;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

	//==========================管理端用户管理===============================


	PageResult<UserVO> pageUsers(long current, long size, String keyword);

	void updateUserStatus(Long id, Integer status);


	//==========================管理端员工管理===============================
	//注册员工(admin)
	void registerAdmin(@Valid EmployeeRegisterDTO dto);

	//分页查询员工(admin)列表
	PageResult<UserVO> pageAdmins(long current, long size, String keyword, Long excludeId);

	/**
	 * 老板删除员工（仅 role=管理员），不可删自己
	 */
	void deleteAdminEmployee(Long operatorId, Long targetId);

	/**
	 * 老板启用/禁用员工账号，不可操作自己
	 */
	void updateAdminEmployeeStatus(Long operatorId, Long targetId, Integer status);


}
