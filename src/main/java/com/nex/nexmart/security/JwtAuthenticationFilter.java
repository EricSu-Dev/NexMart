package com.nex.nexmart.security;

import com.nex.nexmart.common.constant.UserRoleConstants;
import com.nex.nexmart.model.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 请求过滤器
 * 每次请求进来时：
 *   1. 从 Header 取出 Token
 *   2. 验证并解析 Token
 *   3. 把用户信息写入 SecurityContext，后续接口就知道"是谁在请求"
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtUtil jwtUtil;

    private static final String HEADER_NAME  = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

	//@NonNull 主要是为了解决空指针异常,请求、响应、过滤器链是核心组件,任何一个组件都不可以null
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
	    log.info("JWT Filter 收到请求: {}", request.getRequestURI());
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            try {
                Long   userId   = jwtUtil.getUserId(token);
                String username = jwtUtil.getUsername(token);
                String role     = jwtUtil.getRole(token);
	            log.info("从token解析出的role: {}", role);
                // 构建一个极简的 User 对象，避免数据库查询
                User user = new User();
                user.setId(userId);
                user.setUsername(username);
                
                int roleInt = 0;
                if (UserRoleConstants.STRING_ROLE_BOSS.equals(role)) {
                    roleInt = UserRoleConstants.ROLE_BOSS;
                } else if (UserRoleConstants.STRING_ROLE_ADMIN.equals(role)) {
                    roleInt = UserRoleConstants.ROLE_ADMIN;
                }
                user.setRole(roleInt);
                user.setStatus(1); // 既然 Token 合法，状态默认正常

                SecurityUserDetails userDetails = new SecurityUserDetails(user);

                // 构建认证对象，放入 SecurityContext
	            //authentication是认证结果的容器,它不仅包含用户信息，还包含认证状态（是否已登录）和请求详情
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT 认证通过 → userId={}, username={}, role={}", userId, username, role);

            } catch (Exception e) {
                log.warn("JWT 解析异常: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
		// 放行
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取 Token 字符串（去掉 "Bearer " 前缀）
     */
    private String resolveToken(HttpServletRequest request) {
		//token的格式: Authorization: Bearer <token>
        String bearerToken = request.getHeader(HEADER_NAME);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
			//TOKEN_PREFIX.length()为7
	        // substring(7)表示：从第 7 个位置（下标从 0 开始计算）开始，一直截取到字符串的末尾
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
