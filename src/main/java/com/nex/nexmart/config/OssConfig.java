package com.nex.nexmart.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.nex.nexmart.config.properties.AliOssProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfig {
    /**
     * 注册 OSS 客户端为 Spring Bean，全局复用
     */
    @Bean
    @ConditionalOnMissingBean // 如果容器中没有 AliOssUtil 的 Bean，才创建（防止重复创建）
    public OSS ossClient(AliOssProperties aliOssProperties) {
        return new OSSClient(
				aliOssProperties.getEndpoint(),
				aliOssProperties.getAccessKeyId(),
				aliOssProperties.getAccessKeySecret()
        );
    }
}
