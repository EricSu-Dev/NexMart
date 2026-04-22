package com.nex.nexmart.model.vo.cs;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CsSessionVO {
	private Long sessionId;
	private Long userId;
	private String username;    // 用户名
	private String avatar;      // 头像
	private String lastMessage; // 最后一条消息内容
	private Integer unreadCount;// 未读消息数
	private LocalDateTime createdAt;
}
