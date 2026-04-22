package com.nex.nexmart.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "alipay")
//把配置文件里的属性映射到这个类中
public class AlipayProperties {

    /** 支付宝开放平台 AppId */
    private String appId;

    /** 应用私钥 */
    private String privateKey;

    /** 支付宝公钥 */
    private String alipayPublicKey;

    /** 网关地址（沙箱用 openapi.alipaydev.com） */
    private String gatewayUrl;

    /** 异步通知地址（支付宝回调你的服务器） */
    private String notifyUrl;

    /** 同步跳转地址（支付完成后跳转前端页面） */
    private String returnUrl;

    /** 编码 */
    private String charset = "UTF-8";

    /** 签名类型 */
    private String signType = "RSA2";

    /** 格式 */
    private String format = "json";
}
