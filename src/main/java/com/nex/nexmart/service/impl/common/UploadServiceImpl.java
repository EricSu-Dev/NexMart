package com.nex.nexmart.service.impl.common;

import com.aliyun.oss.OSS;
import com.nex.nexmart.config.properties.AliOssProperties;
import com.nex.nexmart.exception.BusinessException;
import com.nex.nexmart.service.intf.common.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传服务实现
 * 负责将图片/视频文件上传到阿里云 OSS，并返回可访问的文件 URL
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UploadServiceImpl implements UploadService {

	// 阿里云 OSS 客户端，用于执行实际的文件上传操作
	private final OSS ossClient;

	// OSS 相关配置（bucket名称、访问域名等）
	private final AliOssProperties aliOssProperties;

	/**
	 * 上传图片或视频文件到 OSS
	 *
	 * @param file 前端传来的文件
	 * @return 文件上传成功后的访问 URL
	 */
	@Override
	public String uploadImage(MultipartFile file) {

		// 1. 校验文件不能为空
		if (file.isEmpty()) {
			throw new BusinessException("文件不能为空");
		}

		// 2. 校验文件类型，只允许图片或视频
		String contentType = file.getContentType();
		if (contentType == null || !(contentType.startsWith("image/") || contentType.startsWith("video/"))) {
			throw new BusinessException("只允许上传图片/视频文件");
		}

		// 3. 提取原始文件的后缀名（如 .jpg .mp4），保留格式信息
		String originalFilename = file.getOriginalFilename();
		String extension = "";
		if (originalFilename != null && originalFilename.contains(".")) {
			//lastIndexOf("."):从字符串中找到最后一个 . 的下标位置并返回
			//substring(index),包含index, 结果 → ".jpg"
			extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		}

		// 4. 用 UUID 生成唯一文件名，避免重名覆盖，统一存放在 media/ 目录下
		// 例如：media/a1b2c3d4e5f6...jpg  UUID 是一种全球唯一的随机标识符，每次调用都会生成一个不同的值
		//UUID.randomUUID():：550e8400-e29b-41d4-a716-446655440000
		//.replace("-", ""),把字符串中所有的 - 替换为空字符串（即删除）,550e8400e29b41d4a716446655440000
		String objectKey = "media/" + UUID.randomUUID().toString().replace("-", "") + extension;

		try {
			// 5. 将文件上传到 OSS 指定的 bucket 中
			ossClient.putObject(aliOssProperties.getBucketName(), objectKey, file.getInputStream());

			// 6. 拼接完整访问 URL，例如：https://xxx.oss-cn-hangzhou.aliyuncs.com/media/xxx.jpg
			String fileUrl = aliOssProperties.getUrlPrefix() + objectKey;
			log.info("OSS 文件上传成功: {}", fileUrl);
			return fileUrl;

		} catch (IOException e) {
			log.error("OSS 文件上传失败", e);
			throw new BusinessException("媒体上传失败，请稍后重试");
		}
	}
}
