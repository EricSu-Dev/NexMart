package com.nex.nexmart.controller.spring.ai;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.model.entity.ai.AiMessage;
import com.nex.nexmart.model.vo.AiSearchSuggestVO;
import com.nex.nexmart.security.SecurityUserDetails;
import com.nex.nexmart.service.intf.ai.history.AiMessageService;
import com.nex.nexmart.service.ai.AiChatService;
import com.nex.nexmart.service.ai.AiSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Tag(name = "AI小助手")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

	private final AiChatService aiChatService;
	private final AiSearchService aiSearchService;
	private final AiMessageService aiMessageService;


	@GetMapping(value = "/chat", produces = "text/event-stream;charset=UTF-8")
	@Operation(summary = "获取对话")
	public Flux<String> chat(@RequestParam String message,
	                         @AuthenticationPrincipal SecurityUserDetails userDetails) {
		Long userId = userDetails.getUser().getId();
		return aiChatService.chat(userId, message);
	}

	// 清空对话历史
	@DeleteMapping("/chat/history")
	@Operation(summary = "清空对话历史")
	public Result<Void> clearHistory(@AuthenticationPrincipal SecurityUserDetails userDetails) {
		aiMessageService.clearHistory(userDetails.getUser().getId());
		return Result.success();
	}

	@GetMapping("/chat/history")
	@Operation(summary = "获取对话历史")
	public Result<List<AiMessage>> getHistory(
			@AuthenticationPrincipal SecurityUserDetails userDetails) {
		return Result.success(aiMessageService.getHistoryByUserId(userDetails.getUser().getId()));
	}

	@GetMapping("/search-suggest")
	@Operation(summary = "搜索建议")
	public Result<AiSearchSuggestVO> searchSuggest(@RequestParam String keyword) {
		return Result.success(aiSearchService.suggest(keyword));
	}

}
