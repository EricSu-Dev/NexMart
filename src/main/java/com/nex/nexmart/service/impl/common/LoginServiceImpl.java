package com.nex.nexmart.service.impl.common;

import com.nex.nexmart.common.constant.UserRoleConstants;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.model.dto.login.LoginDTO;
import com.nex.nexmart.model.dto.login.ResetPasswordDTO;
import com.nex.nexmart.model.dto.login.UserRegisterDTO;
import com.nex.nexmart.model.entity.User;
import com.nex.nexmart.model.vo.LoginVO;
import com.nex.nexmart.model.vo.UserVO;
import com.nex.nexmart.security.JwtUtil;
import com.nex.nexmart.security.LoginUserResolver;
import com.nex.nexmart.service.intf.common.LoginService;
import com.nex.nexmart.service.intf.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

	private final JwtUtil jwtUtil;
	private  final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final LoginUserResolver loginUserResolver;
	private final AuthenticationManager authenticationManager;
	private final StringRedisTemplate stringRedisTemplate;

	private static final Pattern OPTIONAL_EMAIL =
			Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	// 模拟验证码存储：手机号 -> 验证码
	private static final String SMS_CODE_PREFIX = "NexMart:sms:code:";
	private static final long CODE_TTL_MINUTES = 5;
	@Override
	public void register(@Valid UserRegisterDTO dto) {
		String username = dto.getUsername().trim();
		String phone = dto.getPhone() == null ? "" : dto.getPhone().trim();
		if (!StringUtils.hasText(phone)) {
			throw new BusinessException("手机号不能为空");
		}
		if (userService.lambdaQuery().eq(User::getUsername, username).count() > 0) {
			throw new BusinessException("用户名已存在");
		}
		if (userService.lambdaQuery().eq(User::getPhone, phone).count() > 0) {
			throw new BusinessException("手机号已存在");
		}

		String emailRaw = dto.getEmail() == null ? "" : dto.getEmail().trim();
		String emailToSave = StringUtils.hasText(emailRaw) ? emailRaw : null;
		if (emailToSave != null) {
			if (!OPTIONAL_EMAIL.matcher(emailToSave).matches()) {
				throw new BusinessException("邮箱格式错误");
			}
			if (userService.lambdaQuery().eq(User::getEmail, emailToSave).count() > 0) {
				throw new BusinessException("该邮箱已存在");
			}
		}
		User user = new User();
		user.setUsername(username);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setPhone(phone);
		user.setEmail(emailToSave);
		user.setRole(0);
		user.setStatus(1);
		userService.save(user);
	}

	@Override
	public LoginVO login(@Valid LoginDTO dto) {
		// 验证用户和密码是否正确
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(dto.getUsername().trim(), dto.getPassword()));
		} catch (AuthenticationException e) {
			throw new BusinessException("用户名或密码错误");
		}

		User user = loginUserResolver.resolve(dto.getUsername());
		if (user == null) {
			throw new BusinessException("用户不存在");
		}

		String roleStr;
		if (UserRoleConstants.ROLE_BOSS.equals(user.getRole())) {
			roleStr = UserRoleConstants.STRING_ROLE_BOSS;
		} else if (UserRoleConstants.ROLE_ADMIN.equals(user.getRole())) {
			roleStr = UserRoleConstants.STRING_ROLE_ADMIN;
		} else {
			roleStr = UserRoleConstants.STRING_ROLE_USER;
		}

		String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleStr);

		UserVO userVO = new UserVO();
		BeanUtils.copyProperties(user, userVO);

		LoginVO loginVO = new LoginVO();
		loginVO.setToken(token);
		loginVO.setUserInfo(userVO);
		return loginVO;
	}

	@Override
	public UserVO getCurrentUserInfo(Long userId) {
		User user = userService.getById(userId);
		if (user == null) {
			throw new BusinessException("用户不存在");
		}
		UserVO vo = new UserVO();
		BeanUtils.copyProperties(user, vo);
		return vo;
	}

	@Override
	public void sendVerificationCode(String phone){
		String trimmedPhone = resetPasswordVerification(phone);
		// 生成 6 位随机验证码
		String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
		// 用set保存验证码到 Redis,新验证码覆盖旧验证码
		stringRedisTemplate.opsForValue().set(
				SMS_CODE_PREFIX + trimmedPhone,
				code,
				CODE_TTL_MINUTES,
				TimeUnit.MINUTES
		);

		// 日志输出
		log.info("【NexMart】正在为用户 {} 发送重置密码验证码：{}", trimmedPhone, code);
	}

	@Override
	public void resetPassword(@Valid ResetPasswordDTO dto) {
		String trimmedPhone = resetPasswordVerification(dto.getPhone());
		String trimmedCode = dto.getCode().trim();

		// 校验验证码
		String savedCode = stringRedisTemplate.opsForValue().get(SMS_CODE_PREFIX + trimmedPhone);
		if (savedCode == null) {
			throw new BusinessException("验证码已过期或不存在");
		}
		if (!savedCode.equals(trimmedCode)) {
			throw new BusinessException("验证码错误");
		}

		// 更新密码
		boolean success = userService.lambdaUpdate()
				.eq(User::getPhone, trimmedPhone)
				.set(User::getPassword, passwordEncoder.encode(dto.getNewPassword()))
				.update();

		if (success) {
			stringRedisTemplate.delete(SMS_CODE_PREFIX + trimmedPhone);
			log.info("用户 {} 密码重置成功", trimmedPhone);
		} else {
			throw new BusinessException("密码重置失败，请稍后重试");
		}
	}

	private String resetPasswordVerification(String phone) {
		String trimmedPhone = phone.trim();
		// 校验该用户手机号是否存在
		User user = userService.lambdaQuery()
				.eq(User::getPhone, trimmedPhone)
				.one();
		if (user == null) {
			throw new BusinessException("该手机号未注册");
		}
		if (user.getStatus() == 0) {
			throw new BusinessException("该用户已被禁用");
		}
		return trimmedPhone;
	}
}
