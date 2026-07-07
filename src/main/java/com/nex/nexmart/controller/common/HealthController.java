package com.nex.nexmart.controller.common;

import com.nex.nexmart.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

	@Value("${spring.application.name:nexmart}")
	private String applicationName;

	@GetMapping
	public Result<Map<String, Object>> health() {
		return Result.success(Map.of(
				"status", "UP",
				"application", applicationName,
				"time", LocalDateTime.now()
		));
	}
}
