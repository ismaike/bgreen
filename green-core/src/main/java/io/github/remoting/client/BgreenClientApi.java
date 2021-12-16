package io.github.remoting.client;

import io.github.model.NetOptions;
import io.github.model.Result;
import io.github.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author maike
 * @date 2021年12月07日 11:13 下午
 */
@Slf4j
public class BgreenClientApi {

    private static OkHttpClient defaultOkHttpClient = new OkHttpClient().newBuilder()
            .readTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(3, TimeUnit.SECONDS)
            .build();

    public static Result<String> reportMetrics(String jsonContent, NetOptions options) {
        return send("http://127.0.0.1:8080/upload", jsonContent, options);
    }

    public static Result<String> send(String url, String jsonContent, NetOptions options) {

        OkHttpClient requestScope;
        if (defaultOkHttpClient.readTimeoutMillis() != options.readTimeoutMillis() || defaultOkHttpClient.connectTimeoutMillis() != options.connectTimeoutMillis()) {
            requestScope = defaultOkHttpClient.newBuilder()
                    .readTimeout(options.readTimeoutMillis(), TimeUnit.MILLISECONDS)
                    .connectTimeout(options.connectTimeoutMillis(), TimeUnit.MILLISECONDS)
                    .build();
        } else {
            requestScope = defaultOkHttpClient;
        }

        RequestBody jsonBody = RequestBody.create(jsonContent, MediaType.parse("application/json;charset=UTF-8"));

        Request request = new Request.Builder().url(url).post(jsonBody)
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .addHeader("Accept-Charset", "application/json;charset=UTF-8")
                .addHeader("Connection", "Keep-Alive")
                .build();

        try {
            Response response = requestScope.newCall(request).execute();
            if (response.isSuccessful()) {
                log.info("bgreen, remote call success!");
                if (response.body() == null) {
                    return Result.fail("remote call return null!");
                }
                return GsonUtil.fromJson(Objects.requireNonNull(response.body()).string(), Result.class, String.class);
            }
        } catch (IOException e) {
            log.error("bgreen, remote call occur IOException, exception.msg={}", e.getLocalizedMessage());
            return Result.fail("remote call occur IOException");
        }
        log.warn("bgreen, remote call fail!");
        return Result.fail("remote call fail!");
    }

}
