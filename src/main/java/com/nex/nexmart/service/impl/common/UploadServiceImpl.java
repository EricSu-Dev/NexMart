package com.nex.nexmart.service.impl.common;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.nex.nexmart.config.properties.AliOssProperties;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.service.intf.common.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadServiceImpl implements UploadService {

	private static final long MAX_UPLOAD_SIZE = 10 * 1024 * 1024L;
	private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.ofEntries(
			Map.entry(".jpg", "image/jpeg"),
			Map.entry(".jpeg", "image/jpeg"),
			Map.entry(".png", "image/png"),
			Map.entry(".gif", "image/gif"),
			Map.entry(".webp", "image/webp"),
			Map.entry(".mp4", "video/mp4"),
			Map.entry(".webm", "video/webm")
	);

	private final OSS ossClient;
	private final AliOssProperties aliOssProperties;

	@Override
	public String uploadImage(MultipartFile file) {
		validateBasicFileInfo(file);

		String extension = getSafeExtension(file.getOriginalFilename());
		String expectedContentType = ALLOWED_CONTENT_TYPES.get(extension);
		if (expectedContentType == null) {
			throw new BusinessException("仅支持 jpg、jpeg、png、gif、webp、mp4、webm 格式");
		}

		String actualContentType = normalizeContentType(file.getContentType());
		if (!expectedContentType.equals(actualContentType)) {
			throw new BusinessException("文件类型与后缀不匹配");
		}

		try {
			byte[] bytes = file.getBytes();
			if (!matchesMagicNumber(bytes, extension)) {
				throw new BusinessException("文件内容与类型不匹配");
			}

			String objectKey = "media/" + UUID.randomUUID().toString().replace("-", "") + extension;
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(bytes.length);
			metadata.setContentType(expectedContentType);

			ossClient.putObject(
					aliOssProperties.getBucketName(),
					objectKey,
					new ByteArrayInputStream(bytes),
					metadata);

			String fileUrl = aliOssProperties.getUrlPrefix() + objectKey;
			log.info("OSS file uploaded successfully: {}", fileUrl);
			return fileUrl;
		} catch (IOException e) {
			log.error("OSS file upload failed", e);
			throw new BusinessException("媒体上传失败，请稍后重试");
		}
	}

	private void validateBasicFileInfo(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException("文件不能为空");
		}
		if (file.getSize() > MAX_UPLOAD_SIZE) {
			throw new BusinessException("文件大小不能超过 10MB");
		}
		if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
			throw new BusinessException("文件名不能为空");
		}
		if (file.getContentType() == null || file.getContentType().isBlank()) {
			throw new BusinessException("文件类型不能为空");
		}
	}

	private String getSafeExtension(String originalFilename) {
		int dotIndex = originalFilename.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
			throw new BusinessException("文件后缀不能为空");
		}
		return originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);
	}

	private String normalizeContentType(String contentType) {
		int semicolonIndex = contentType.indexOf(';');
		String normalized = semicolonIndex >= 0 ? contentType.substring(0, semicolonIndex) : contentType;
		return normalized.trim().toLowerCase(Locale.ROOT);
	}

	private boolean matchesMagicNumber(byte[] bytes, String extension) {
		return switch (extension) {
			case ".jpg", ".jpeg" -> startsWith(bytes, 0xFF, 0xD8, 0xFF);
			case ".png" -> startsWith(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
			case ".gif" -> startsWithAscii(bytes, "GIF87a") || startsWithAscii(bytes, "GIF89a");
			case ".webp" -> bytes.length >= 12
					&& startsWithAscii(bytes, "RIFF")
					&& asciiEquals(bytes, 8, "WEBP");
			case ".mp4" -> bytes.length >= 12 && asciiEquals(bytes, 4, "ftyp");
			case ".webm" -> startsWith(bytes, 0x1A, 0x45, 0xDF, 0xA3);
			default -> false;
		};
	}

	private boolean startsWith(byte[] bytes, int... expected) {
		if (bytes.length < expected.length) {
			return false;
		}
		for (int i = 0; i < expected.length; i++) {
			if ((bytes[i] & 0xFF) != expected[i]) {
				return false;
			}
		}
		return true;
	}

	private boolean startsWithAscii(byte[] bytes, String expected) {
		return asciiEquals(bytes, 0, expected);
	}

	private boolean asciiEquals(byte[] bytes, int offset, String expected) {
		if (bytes.length < offset + expected.length()) {
			return false;
		}
		for (int i = 0; i < expected.length(); i++) {
			if (bytes[offset + i] != (byte) expected.charAt(i)) {
				return false;
			}
		}
		return true;
	}
}
