package tech.z9n.ddns.task;

import com.alibaba.fastjson.JSON;
import com.aliyun.alidns20150109.Client;
import com.aliyun.alidns20150109.models.*;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tech.z9n.ddns.config.AliyunSdkConfig;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class DynamicDnsTask {
    @Autowired
    private Client client;

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private AliyunSdkConfig config;

    @PostConstruct
    public void init() {
        if (config.isResolveOnBoot()) {
            execute();
        }
    }

    @Scheduled(cron = "${aliyun.sdk.config.cron:0 0/10 * * * ?}")
    public synchronized void execute() {
        long startTime = System.currentTimeMillis();
        log.info("开始执行动态域名解析");

        try {
            ddns();
        } catch (Exception e) {
            log.error("动态域名解析执行异常", e);
        }

        log.info("执行动态域名解析结束耗时:{}ms", System.currentTimeMillis() - startTime);
    }

    private void ddns() throws Exception {
        // 查询当前ip
        String currentHostIp = getCurrentHostIp();

        // 查询当前解析的ip
        DescribeDomainRecordsResponseBodyDomainRecordsRecord record = getCurrentDnsRecord();

        // 检查是否需要修改
        if (currentHostIp.equals(record.getValue())) {
            log.info("域名: {}, 查询当前ip与dns解析ip相同 无需修改, ip: {}", config.getDomainName(), currentHostIp);
            return;
        }

        // 执行修改操作
        updateDnsIp(record, currentHostIp);
    }

    private String getCurrentHostIp() throws Exception {
        log.info("开始查询当前公网ip");
        String urlString = "https://jsonip.com";
        try {
            Call call = okHttpClient.newCall(new Request.Builder()
                    .url(urlString)
                    .method("GET", null)
                    .build());
            try (Response response = call.execute()) {
                String jsonResp = response.body().string();
                log.info("调用{}结果: {}", urlString, jsonResp);
                return JSON.parseObject(jsonResp).getString("ip");
            }
        } catch (Exception e) {
            log.error("查询当前ip异常", e);
            throw e;
        }
    }

    private DescribeDomainRecordsResponseBodyDomainRecordsRecord getCurrentDnsRecord() throws Exception {
        log.info("开始查询当前dns信息");
        RuntimeOptions runtime = new RuntimeOptions();

        DescribeDomainRecordsRequest describeDomainRecordsRequest = new DescribeDomainRecordsRequest();
        describeDomainRecordsRequest.setDomainName(config.getDomainName());
        DescribeDomainRecordsResponse describeDomainRecordsResponse = client.describeDomainRecordsWithOptions(describeDomainRecordsRequest, runtime);

        DescribeDomainRecordsResponseBodyDomainRecordsRecord record = describeDomainRecordsResponse.getBody().getDomainRecords().getRecord().get(0);
        log.info("查询到的dns信息：{}", JSON.toJSONString(record));

        return record;
    }

    private void updateDnsIp(DescribeDomainRecordsResponseBodyDomainRecordsRecord record, String newIp) throws Exception {
        log.info("开始更新当前dns信息, record: {}, newIp: {}", JSON.toJSONString(record), newIp);
        RuntimeOptions runtime = new RuntimeOptions();

        UpdateDomainRecordRequest updateDomainRecordRequest = new UpdateDomainRecordRequest()
                .setRecordId(record.getRecordId())
                .setRR(record.getRR())
                .setType(record.getType())
                .setValue(newIp);
        client.updateDomainRecordWithOptions(updateDomainRecordRequest, runtime);
    }
}
