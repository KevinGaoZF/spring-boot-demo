package net.esati.spider.web.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * @author Gao
 * @version 1.0
 * @description: TODO
 * @date 2023/8/16 15:52
 */
@Slf4j
public class HttpClientUtil {
    /**
     * 解决接口地址自签名证书不受信任的情况
     * <p>
     * This is very bad practice and should NOT be used in production.
     *
     * @return OkHttpClient
     */
    private static OkHttpClient getUnsafeOkHttpClient(Boolean isUseProxy,String proxyHost,Integer proxyPort) {
        return getOkHttpClient(isUseProxy, proxyHost, proxyPort);
    }

    @NotNull
    public static OkHttpClient getOkHttpClient(Boolean isUseProxy, String proxyHost, Integer proxyPort) {
        try {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(50, 2, TimeUnit.MINUTES));

            OkHttpClient okHttpClient;
            if(isUseProxy) {
                // 创建代理
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                // 设置代理到OkHttpClient
                okHttpClient = builder.proxy(proxy).build();
            } else {
                okHttpClient = builder.build();
            }
//            log.info("创建 okhttp client 是否启用代理：{} 代理信息配置完成：host: {} port : {} ", isUseProxy,proxyHost,proxyPort);
            return okHttpClient;
        } catch (Exception e) {
            log.error("okhttp client build error!");
            throw new RuntimeException(e);
        }
    }

    public static JSONObject requestGetUrl(String url,Boolean isUseProxy, String proxyHost, Integer proxyPort){
        JSONObject responseJson = new JSONObject();
        // 创建OkHttpClient，并设置SSL上下文
        OkHttpClient client =getOkHttpClient(isUseProxy,proxyHost,proxyPort);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            String rspStr = response.body().string();
            responseJson = JSONObject.parseObject(rspStr);
        } catch (IOException e) {
            log.error("通用GET请求URL: {} 异常: {}",url,e);
            e.printStackTrace();

        }
        return responseJson;
    }
    
    public static String requestGetUrlStr(String url,Boolean isUseProxy, String proxyHost, Integer proxyPort,Integer maxAttempts ){
        // 创建OkHttpClient，并设置SSL上下文
        String rspStr = null;
        OkHttpClient client =getOkHttpClient(isUseProxy,proxyHost,proxyPort);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Response response = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {

                // 检查响应是否成功
                response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    // 处理响应
                    rspStr = response.body().string();
                    break;  // 如果成功，跳出循环
                }


            } catch (IOException e) {
                log.error("通用GET请求URL: {} 异常: {}",url,e);
                e.printStackTrace();
            }
            // 如果尝试次数小于最大尝试次数，等待一段时间后重试
            if (attempt < maxAttempts) {
                int waitTimeInSeconds = 5;  // 设置等待时间（秒）
               log.info("Retrying in " + waitTimeInSeconds + " seconds...");
                try {
                    Thread.sleep(waitTimeInSeconds * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                log.info("Max attempts reached. Exiting...");
            }
        }
        return rspStr;
    }
}
