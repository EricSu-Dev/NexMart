package com.nex.nexmart.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 根据 secret 生成签名 Key
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     角色（ROLE_USER / ROLE_ADMIN）
     */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expiration);

        return Jwts.builder()
		        //唯一标识符(用户ID)
                .subject(String.valueOf(userId))
		        //自定义存入的数据
                .claim("username", username)
                .claim("role", role)
		        // 签发时间
                .issuedAt(now)
		        // 过期时间
                .expiration(expireAt)
		        // 签名
                .signWith(getSigningKey())
		        // 生成 Token
                .compact();
    }

    /**
     * 解析 Token，返回 Claims（包含所有载荷信息）
     * 解析失败（过期 / 篡改 / 格式错误）抛出 JwtException
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
		        // 验证签名来判断是否被篡改
                .verifyWith(getSigningKey())
		        // 验证格式及是否过期
                .build().parseSignedClaims(token)
		        // 返回 Claims
                .getPayload();
    }


	/**
	 * 校验 Token 是否有效
	 */
	public boolean validateToken(String token) {
		try {
			parseToken(token);
			//解析成功,返回 true
			return true;
			//解析过程中出现任何错误，都返回 false
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("无效的 JWT Token: {}", e.getMessage());
			return false;
		}
	}



    /**
     * 从 Token 中取用户 ID
     */
    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    /**
     * 从 Token 中取用户名
     */
    public String getUsername(String token) {
		//加上String.class代表把Object自动转为String
        return parseToken(token).get("username", String.class);
    }

    /**
     * 从 Token 中取角色
     */
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }


}
