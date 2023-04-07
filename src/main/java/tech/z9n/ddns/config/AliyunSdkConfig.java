package tech.z9n.ddns.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.sdk.config")
public class AliyunSdkConfig {
    private String domainName;
    private String accessKeyId;
    private String accessKeySecret;
    private boolean resolveOnBoot = true;
}
