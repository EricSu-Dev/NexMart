package com.nex.nexmart.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.common.ResultCode;
import com.nex.nexmart.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // 开启 @PreAuthorize 注解权限控制
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
	@Value("${app.cors.allowed-origin-patterns:http://localhost:5173,http://127.0.0.1:5173,https://nexmart.tech}")
	private String allowedOriginPatterns;

    /**
     * 密码加密器（BCrypt）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器（登录时用）
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 核心安全过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// 1. 关闭 CSRF（前后端分离，用 JWT 不需要 CSRF）
	    http.csrf(AbstractHttpConfigurer::disable)

            // 2. 跨域配置,前后端端口不一致时浏览器也能放行
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 3. 无状态 Session（JWT 模式）不需要Session
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 4. 接口权限规则
            .authorizeHttpRequests(auth -> auth
		            // 公开接口：无需登录
		            .requestMatchers(
				            "/ws/**",                          // WebSocket
				            "/webjars/**",                     // Swagger UI 等静态资源
				            "/swagger-ui/**",                  // Swagger UI
				            "/v3/api-docs/**",                 // Swagger API 文档
				            "/swagger-ui.html",                // Swagger UI HTML 页面
				            "/api/health",                     // 健康检查
				            "/api/user/product/**",            // 商品查询公开
				            "/api/user/category/**",           // 分类查询公开
				            "/api/ai/search-suggest",          // AI搜索功能
				            "/api/common/auth/login",          // 登录
				            "/api/user/payment/return",        // 支付宝同步跳转，必须公开
				            "/api/user/payment/notify",        // 支付宝异步回调，必须公开
				            "/api/common/auth/register",       // 注册
				            "/api/common/auth/reset-code",     // 发送验证码
				            "/api/common/auth/reset-password"  // 重置密码
		            ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/user/reviews/**").permitAll()
                // 管理员/老板 专属接口
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "BOSS")
                // 公共接口（需登录）
                .requestMatchers("/api/common/**").authenticated()
                // 其余接口登录即可访问
                .anyRequest().authenticated()
            )

            // 5. 未登录时返回 JSON（而不是跳转登录页）
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
                    response.getWriter().write(
                        objectMapper.writeValueAsString(Result.fail(ResultCode.UNAUTHORIZED))
                    );
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
                    response.getWriter().write(
                        objectMapper.writeValueAsString(Result.fail(ResultCode.FORBIDDEN))
                    );
                })
            )

            // 6. 把 JWT 过滤器加在 UsernamePassword 过滤器之前
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 跨域配置（允许前端开发服务器访问）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.stream(allowedOriginPatterns.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toList());
	    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
