package com.nex.nexmart.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nex.nexmart.common.constant.UserRoleConstants;
import com.nex.nexmart.common.PageResult;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.mapper.base.UserMapper;
import com.nex.nexmart.model.dto.login.*;
import com.nex.nexmart.model.entity.User;
import com.nex.nexmart.model.vo.UserVO;
import com.nex.nexmart.service.intf.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

	private final PasswordEncoder passwordEncoder;
	private static final Pattern OPTIONAL_EMAIL =
			Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

//===================================以下为用户管理模块接口================================

	@Override
	public PageResult<UserVO> pageUsers(long current, long size, String keyword) {
		Page<User> page = lambdaQuery()
				.eq(User::getRole, 0)
				.like(StringUtils.hasText(keyword), User::getUsername, keyword)
				.orderByDesc(User::getCreatedAt)
				.page(new Page<>(current, size));

		return PageResult.of(page.convert(user -> {
			UserVO vo = new UserVO();
			BeanUtils.copyProperties(user, vo);
			return vo;
		}));
	}

	@Override
	public void updateUserStatus(Long id, Integer status) {
		if (status == null || (status != 0 && status != 1)) {
			throw new BusinessException("状态值非法");
		}
		User user = getById(id);
		if (user == null) {
			throw new BusinessException("用户不存在");
		}
		if (user.getRole() == UserRoleConstants.ROLE_ADMIN || user.getRole() == UserRoleConstants.ROLE_BOSS) {
			throw new BusinessException("不能修改管理人员状态");
		}

		lambdaUpdate().eq(User::getId, id)
				.set(User::getStatus, status)
				.update();
	}

	//===================================以下为员工管理模块接口================================
	@Override
	public void registerAdmin(@Valid EmployeeRegisterDTO dto) {
		if (lambdaQuery().eq(User::getUsername, dto.getUsername().trim()).count() > 0) {
			throw new BusinessException("用户名已存在");
		}
		//.trim()去除字符串首尾的空白字符
		String phone = dto.getPhone().trim();
		if (lambdaQuery().eq(User::getPhone, phone).count() > 0) {
			throw new BusinessException("手机号已被使用");
		}
		String email = dto.getEmail() == null ? "" : dto.getEmail().trim();
		if (StringUtils.hasText(email)) {
			//.matcher(email)把正则表达式和要检测的字符串 email 绑定在一起,返回一个 Matcher 对象（匹配器），还没有开始匹配
			//.matches()执行匹配，判断整个字符串是否完全符合正则表达式
			if (!OPTIONAL_EMAIL.matcher(email).matches()) {
				throw new BusinessException("邮箱格式不正确");
			}
			if (lambdaQuery().eq(User::getEmail, email).count() > 0) {
				throw new BusinessException("邮箱已被注册");
			}
		}

		User user = new User();
		user.setUsername(dto.getUsername().trim());
		//对密码进行加密，返回加密后的字符串
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setPhone(phone);
		user.setEmail(StringUtils.hasText(email) ? email : null);
		user.setRole(UserRoleConstants.ROLE_ADMIN);
		user.setStatus(1);
		save(user);
	}


	@Override
	public PageResult<UserVO> pageAdmins(long current, long size, String keyword, Long excludeId) {
		Page<User> page = lambdaQuery()
				.eq(User::getRole, UserRoleConstants.ROLE_ADMIN) // 只查员工
				.ne(excludeId != null, User::getId, excludeId)
				.like(StringUtils.hasText(keyword), User::getUsername, keyword)
				.orderByDesc(User::getCreatedAt)
				.page(new Page<>(current, size));

		return PageResult.of(page.convert(user -> {
			UserVO vo = new UserVO();
			BeanUtils.copyProperties(user, vo);
			return vo;
		}));
	}

	@Override
	public void deleteAdminEmployee(Long operatorId, Long targetId) {
		if (operatorId.equals(targetId)) {
			throw new BusinessException("不能删除自己的账号");
		}
		User target = getById(targetId);
		if (target == null) {
			throw new BusinessException("员工不存在");
		}
		if (target.getRole() != UserRoleConstants.ROLE_ADMIN) {
			throw new BusinessException("只能删除员工账号");
		}
		removeById(targetId);
	}

	@Override
	public void updateAdminEmployeeStatus(Long operatorId, Long targetId, Integer status) {
		if (operatorId.equals(targetId)) {
			throw new BusinessException("不能修改自己的状态");
		}
		if (status == null || (status != 0 && status != 1)) {
			throw new BusinessException("状态值非法");
		}
		User target = getById(targetId);
		if (target == null) {
			throw new BusinessException("员工不存在");
		}
		if (target.getRole() != UserRoleConstants.ROLE_ADMIN) {
			throw new BusinessException("只能操作员工账号");
		}
		lambdaUpdate().eq(User::getId, targetId)
				.set(User::getStatus, status)
				.update();
	}
}
