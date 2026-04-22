package com.nex.nexmart.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String avatarUrl;
    private Integer role;
    private Integer status;
    private LocalDateTime createdAt;
	private String profileSignature;
}
