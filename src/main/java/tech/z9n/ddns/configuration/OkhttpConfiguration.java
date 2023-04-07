package tech.z9n.ddns.configuration;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.z9n.ddns.config.AliyunSdkConfig;

@Configuration
public class OkhttpConfiguration {
    @Bean
    public OkHttpClient okHttpClient() throws Exception {
        return new OkHttpClient.Builder()
                .build();
    }
}
