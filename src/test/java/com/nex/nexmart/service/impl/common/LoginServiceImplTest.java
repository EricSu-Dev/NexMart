package com.nex.nexmart.service.impl.common;

import com.nex.nexmart.common.constant.UserRoleConstants;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.model.dto.login.LoginDTO;
import com.nex.nexmart.model.entity.User;
import com.nex.nexmart.model.vo.LoginVO;
import com.nex.nexmart.security.JwtUtil;
import com.nex.nexmart.security.LoginUserResolver;
import com.nex.nexmart.service.intf.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

	@Mock
	private JwtUtil jwtUtil;
	@Mock
	private UserService userService;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private LoginUserResolver loginUserResolver;
	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@InjectMocks
	private LoginServiceImpl loginService;

	@Test
	void loginReturnsTokenAndUserInfoWhenCredentialsAreValid() {
		LoginDTO dto = new LoginDTO();
		dto.setUsername(" eric ");
		dto.setPassword("secret");
		User user = new User();
		user.setId(1L);
		user.setUsername("eric");
		user.setRole(UserRoleConstants.ROLE_ADMIN);
		user.setStatus(1);

		when(authenticationManager.authenticate(any())).thenReturn(org.mockito.Mockito.mock(Authentication.class));
		when(loginUserResolver.resolve(" eric ")).thenReturn(user);
		when(jwtUtil.generateToken(1L, "eric", UserRoleConstants.STRING_ROLE_ADMIN)).thenReturn("jwt-token");

		LoginVO result = loginService.login(dto);

		assertThat(result.getToken()).isEqualTo("jwt-token");
		assertThat(result.getUserInfo().getId()).isEqualTo(1L);
		assertThat(result.getUserInfo().getUsername()).isEqualTo("eric");
		verify(authenticationManager).authenticate(any());
		verify(jwtUtil).generateToken(1L, "eric", UserRoleConstants.STRING_ROLE_ADMIN);
	}

	@Test
	void loginThrowsBusinessExceptionWhenAuthenticationFails() {
		LoginDTO dto = new LoginDTO();
		dto.setUsername("eric");
		dto.setPassword("wrong-password");

		when(authenticationManager.authenticate(any()))
				.thenThrow(new BadCredentialsException("bad credentials"));

		assertThatThrownBy(() -> loginService.login(dto))
				.isInstanceOf(BusinessException.class);
		verify(authenticationManager).authenticate(any());
	}
}
