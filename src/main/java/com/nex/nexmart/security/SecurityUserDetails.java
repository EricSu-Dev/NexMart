package com.nex.nexmart.security;

import com.nex.nexmart.common.constant.UserRoleConstants;
import com.nex.nexmart.model.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security 用户信息包装类
 * 把数据库的 User 实体适配成 Security 需要的 UserDetails 接口
 */
@Getter
public class SecurityUserDetails implements UserDetails {

    /** 原始用户实体，方便在业务层取 userId 等信息 */
    private final User user;

    public SecurityUserDetails(User user) {
        this.user = user;
    }

    /**
     * 返回权限列表
     * role=0 → ROLE_USER
     * role=1 → ROLE_ADMIN
     */
	//给 Spring Security 框架用
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleStr;
        if (user.getRole() .equals(UserRoleConstants.ROLE_BOSS)) {
            roleStr = UserRoleConstants.STRING_ROLE_BOSS;
        } else if (user.getRole().equals(UserRoleConstants.ROLE_ADMIN)) {
            roleStr = UserRoleConstants.STRING_ROLE_ADMIN;
        } else {
            roleStr = UserRoleConstants.STRING_ROLE_USER;
        }
        return List.of(new SimpleGrantedAuthority(roleStr));
    }

	//给 Spring Security 框架用
    @Override
    public String getPassword() {
        return user.getPassword();
    }

	//给 Spring Security 框架用
    @Override
    public String getUsername() {
        return user.getUsername();
    }



    /** 账号是否未锁定 */
	//如果用户登录时被封了,那么就算jwt没有过期,登录接口也会返回失败
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() == 1;
    }

	/** 账号是否可用 */
	@Override
	public boolean isEnabled() {
		return user.getStatus() == 1;
	}

	/** 账号是否未过期（用 status 字段控制） */
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

    /** 凭证是否未过期 */
	//本项目暂不需要验证凭证过期,直接返回 true
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }


}
