package com.nex.nexmart.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.nex.nexmart.config.properties.AlipayProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlipayConfig {

	/**
	 * 注册支付宝客户端为 Spring Bean，全局复用
	 * 避免每次支付请求都重新创建客户端对象
	 */
	@Bean
	@ConditionalOnMissingBean
	public AlipayClient alipayClient(AlipayProperties alipayProperties) {
		return new DefaultAlipayClient(
				alipayProperties.getGatewayUrl(),
				alipayProperties.getAppId(),
				alipayProperties.getPrivateKey(),
				alipayProperties.getFormat(),
				alipayProperties.getCharset(),
				alipayProperties.getAlipayPublicKey(),
				alipayProperties.getSignType()
		);
	}
}