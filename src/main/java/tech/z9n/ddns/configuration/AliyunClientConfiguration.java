package tech.z9n.ddns.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.z9n.ddns.config.AliyunSdkConfig;

@Configuration
public class AliyunClientConfiguration {
    @Autowired
    private AliyunSdkConfig aliyunSdkConfig;

    @Bean
    public com.aliyun.alidns20150109.Client createClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(aliyunSdkConfig.getAccessKeyId())
                .setAccessKeySecret(aliyunSdkConfig.getAccessKeySecret());
        config.endpoint = "alidns.cn-hangzhou.aliyuncs.com";
        return new com.aliyun.alidns20150109.Client(config);
    }
}
