package com.nex.nexmart.service.impl.common;

import com.nex.nexmart.common.constant.UserRoleConstants;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.model.dto.login.ChangePhoneNumberDTO;
import com.nex.nexmart.model.dto.login.PasswordUpdateDTO;
import com.nex.nexmart.model.dto.login.ProfileUpdateDTO;
import com.nex.nexmart.model.entity.User;
import com.nex.nexmart.model.vo.LoginVO;
import com.nex.nexmart.model.vo.UserVO;
import com.nex.nexmart.security.JwtUtil;
import com.nex.nexmart.service.intf.common.ProfileService;
import com.nex.nexmart.service.intf.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
	private final JwtUtil jwtUtil;
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final StringRedisTemplate stringRedisTemplate;
	private static final Pattern OPTIONAL_EMAIL =
			Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	private static final String SMS_CODE_PREFIX = "NexMart:sms:code:";
	private static final long CODE_TTL_MINUTES = 5;
	@Override
	public LoginVO updateProfile(Long userId, Integer role, ProfileUpdateDTO dto) {
		User user = userService.getById(userId);
		if (user == null) {
			throw new BusinessException("用户不存在");
		}

		if (!user.getRole().equals(role)) {
			throw new BusinessException("用户角色不匹配,无权修改信息");
		}

		String username = dto.getUsername().trim();
		String phone = dto.getPhone().trim();
		String emailRaw = dto.getEmail() == null ? "" : dto.getEmail().trim();
		String emailToSave = StringUtils.hasText(emailRaw) ? emailRaw : null;
		String avatarRaw = dto.getAvatarUrl() == null ? "" : dto.getAvatarUrl().trim();
		String avatarToSave = StringUtils.hasText(avatarRaw) ? avatarRaw : null;
		String signature = dto.getProfileSignature() == null ? null : dto.getProfileSignature().trim();

		if (!username.equals(user.getUsername())) {
			if (userService.lambdaQuery().eq(User::getUsername, username).ne(User::getId, userId).count() > 0) {
				throw new BusinessException("用户名已被使用");
			}
		}
		if (emailToSave != null) {
			if (!OPTIONAL_EMAIL.matcher(emailToSave).matches()) {
				throw new BusinessException("邮箱格式不正确");
			}
			String oldEmail = user.getEmail();
			boolean same = oldEmail != null && oldEmail.equalsIgnoreCase(emailToSave);
			if (!same && userService.lambdaQuery().eq(User::getEmail, emailToSave).ne(User::getId, userId).count() > 0) {
				throw new BusinessException("邮箱已被使用");
			}
		}
		String currentPhone = user.getPhone() == null ? "" : user.getPhone();
		if (!phone.equals(currentPhone)) {
			if (userService.lambdaQuery().eq(User::getPhone, phone).ne(User::getId, userId).count() > 0) {
				throw new BusinessException("手机号已被使用");
			}
		}

		userService.lambdaUpdate()
				.eq(User::getId, userId)
				.set(User::getUsername, username)
				.set(User::getEmail, emailToSave)
				.set(User::getPhone, phone)
				.set(User::getAvatarUrl, avatarToSave)
				.set(signature != null, User::getProfileSignature, signature)
				.update();

		User fresh = userService.getById(userId);
		String roleStr;
		if (fresh.getRole().equals(UserRoleConstants.ROLE_BOSS)) {
			roleStr = UserRoleConstants.STRING_ROLE_BOSS;
		} else if (fresh.getRole().equals(UserRoleConstants.ROLE_ADMIN)) {
			roleStr = UserRoleConstants.STRING_ROLE_ADMIN;
		} else {
			roleStr = UserRoleConstants.STRING_ROLE_USER;
		}
		String token = jwtUtil.generateToken(fresh.getId(), fresh.getUsername(), roleStr);
		UserVO userVO = new UserVO();
		BeanUtils.copyProperties(fresh, userVO);
		LoginVO loginVO = new LoginVO();
		loginVO.setToken(token);
		loginVO.setUserInfo(userVO);
		return loginVO;
	}

	@Override
	public void updatePassword(Long userId, Integer role, PasswordUpdateDTO dto) {
		User user = userService.getById(userId);
		if (user == null) {
			throw new BusinessException("用户不存在");
		}

		if (!user.getRole().equals(role)) {
			throw new BusinessException("用户角色不匹配,无权修改信息");
		}

		if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
			throw new BusinessException("旧密码错误");
		}

		if (dto.getNewPassword().equals(dto.getOldPassword())) {
			throw new BusinessException("新密码不能与旧密码相同");
		}

		userService.lambdaUpdate()
				.eq(User::getId, userId)
				.set(User::getPassword, passwordEncoder.encode(dto.getNewPassword()))
				.update();
	}

	@Override
	public void sendVerificationCode(String originalPhone,String newPhone,Long userId){
		//检验
		String trimmedOriginalPhone = changePhoneNumberVerification(originalPhone, newPhone, userId);

		// 生成 6 位随机验证码
		String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
		//存入Redis
		stringRedisTemplate.opsForValue().set(
				SMS_CODE_PREFIX + trimmedOriginalPhone,
				code,
				CODE_TTL_MINUTES,
				TimeUnit.MINUTES
		);

		// 日志输出
		log.info("【NexMart】正在为原手机号 {} 发送更改手机号的验证码：{}", trimmedOriginalPhone, code);
	}

	@Override
	public void changePhoneNumber(@Valid ChangePhoneNumberDTO dto, Long userId){
		String originalPhone = changePhoneNumberVerification(dto.getOriginPhoneNumber(), dto.getNewPhoneNumber(), userId);
		String newPhone = dto.getNewPhoneNumber().trim();

		// 校验验证码
		String savedCode = stringRedisTemplate.opsForValue().get(SMS_CODE_PREFIX + originalPhone);
		if (savedCode == null) {
			throw new BusinessException("验证码已过期或不存在");
		}
		if (!savedCode.equals(dto.getCode().trim())) {
			throw new BusinessException("验证码错误");
		}

		boolean success = userService.lambdaUpdate()
				.eq(User::getPhone, originalPhone)
				.set(User::getPhone, newPhone)
				.update();
		if (success) {
			stringRedisTemplate.delete(SMS_CODE_PREFIX + originalPhone);
			log.info("用户手机号更改成功,已从{}更新成为{}", originalPhone, newPhone);
		} else {
			throw new BusinessException("手机号更改失败，请稍后重试");
		}

	}

	private String changePhoneNumberVerification(String originalPhone,String newPhone,Long userId) {
		String trimmedOriginalPhone = originalPhone.trim();
		String trimmedNewPhone = newPhone.trim();
		// 校验原手机号用户是否存在
		User originUser = userService.lambdaQuery()
				.eq(User::getPhone, trimmedOriginalPhone)
				.one();
		if (originUser == null||!originUser.getId().equals(userId)) {
			throw new BusinessException("原手机号不存在或用户错误");
		}
		User newUser = userService.lambdaQuery()
				.eq(User::getPhone, trimmedNewPhone)
				.one();
		if (newUser != null) {
			throw new BusinessException("要更改的手机号已被注册");
		}

		if (originUser.getStatus() == 0) {
			throw new BusinessException("该用户已被禁用");
		}

		if(originalPhone.equals(newPhone)){
			throw new BusinessException("原手机号不能与要更改的手机号相同");
		}
		return trimmedOriginalPhone;
	}

}
