package top.daoha.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName : AliPayConfig
 * @Description :支付宝沙箱相关配置
 * @github:
 * @Author : 24209
 * @Date: 2026/3/7  20:04
 */

@Configuration  //会将带有@Bean注释的函数的返回值按照名字存入容器中
@EnableConfigurationProperties(AliPayConfigProperties.class)
public class AliPayConfig {
    @Bean("alipayClient")//通过执行方法来获得
    public AlipayClient alipayClient(AliPayConfigProperties properties) {
        return new DefaultAlipayClient(properties.getGatewayUrl(),
                properties.getApp_id(),
                properties.getMerchant_private_key(),
                properties.getFormat(),
                properties.getCharset(),
                properties.getAlipay_public_key(),
                properties.getSign_type());
    }
}
