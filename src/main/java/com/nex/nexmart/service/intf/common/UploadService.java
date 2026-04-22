package com.nex.nexmart.service.intf.common;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务
 */
public interface UploadService {

	String uploadImage(MultipartFile file);
}
