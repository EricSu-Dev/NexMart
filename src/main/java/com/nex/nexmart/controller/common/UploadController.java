package com.nex.nexmart.controller.common;

import com.nex.nexmart.common.Result;
import com.nex.nexmart.service.intf.common.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "通用-文件上传")
@RestController
@RequestMapping("/api/common/upload")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'BOSS')")
public class UploadController {

	private final UploadService uploadService;

	@Operation(summary = "上传图片到阿里云 OSS（通用）")
	@PostMapping("/image")
	public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
		log.info("Common upload image filename={} size={}", file.getOriginalFilename(), file.getSize());
		return Result.success(uploadService.uploadImage(file));
	}
}
