package com.nex.nexmart.controller.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.nexmart.exception.GlobalExceptionHandler;
import com.nex.nexmart.model.dto.login.LoginDTO;
import com.nex.nexmart.model.vo.LoginVO;
import com.nex.nexmart.model.vo.UserVO;
import com.nex.nexmart.service.intf.common.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerIntegrationTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@Mock
	private LoginService loginService;

	@BeforeEach
	void setUp() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		objectMapper = new ObjectMapper();
		mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(loginService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@Test
	void loginEndpointReturnsTokenWhenRequestIsValid() throws Exception {
		UserVO userVO = new UserVO();
		userVO.setId(1L);
		userVO.setUsername("eric");
		when(loginService.login(any(LoginDTO.class)))
				.thenReturn(LoginVO.builder().token("jwt-token").userInfo(userVO).build());

		LoginDTO dto = new LoginDTO();
		dto.setUsername("eric");
		dto.setPassword("secret");

		mockMvc.perform(post("/api/common/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.token").value("jwt-token"))
				.andExpect(jsonPath("$.data.userInfo.username").value("eric"));
	}

	@Test
	void loginEndpointRejectsBlankPasswordBeforeCallingService() throws Exception {
		LoginDTO dto = new LoginDTO();
		dto.setUsername("eric");
		dto.setPassword("");

		mockMvc.perform(post("/api/common/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(422));
	}
}
