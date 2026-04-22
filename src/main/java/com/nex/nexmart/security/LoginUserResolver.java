package com.nex.nexmart.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nex.nexmart.mapper.base.UserMapper;
import com.nex.nexmart.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 登录账号解析：11 位手机号先按手机匹配，否则按用户名匹配（避免用户名与手机号冲突时歧义）
 */
@Component
@RequiredArgsConstructor
public class LoginUserResolver {

    private static final Pattern CN_MOBILE = Pattern.compile("^1[3-9]\\d{9}$");

    private final UserMapper userMapper;

    public User resolve(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String s = raw.trim();
        if (CN_MOBILE.matcher(s).matches()) {
            User byPhone = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, s));
            if (byPhone != null) {
                return byPhone;
            }
        }
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, s));
    }
}
