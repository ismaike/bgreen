package io.github.util;

import io.github.model.Result;
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
public class RemotingUtil {

    private static OkHttpClient defaultOkHttpClient = new OkHttpClient().newBuilder()
            .readTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(3, TimeUnit.SECONDS)
            .build();

    public static Result send(String url, String jsonContent, Class jsonContentClazz, Options options) {

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
                .addHeader("connection", "Keep-Alive")
                .build();

        try {
            Response response = requestScope.newCall(request).execute();
            if (response.isSuccessful()) {
                if (response.body() == null) {
                    return Result.fail("remote call return null");
                }
                return GsonUtil.fromJson(Objects.requireNonNull(response.body()).string(), Result.class, jsonContentClazz);
            }
        } catch (IOException e) {
            return Result.fail("remote call occur IOException");
        }

        return Result.fail("remote call fail");
    }

    public static class Options {

        private final long connectTimeout;
        private final TimeUnit connectTimeoutUnit;
        private final long readTimeout;
        private final TimeUnit readTimeoutUnit;
        private final boolean followRedirects;


        /**
         * Creates a new Options Instance.
         *
         * @param connectTimeout     value.
         * @param connectTimeoutUnit with the TimeUnit for the timeout value.
         * @param readTimeout        value.
         * @param readTimeoutUnit    with the TimeUnit for the timeout value.
         * @param followRedirects    if the request should follow 3xx redirections.
         */
        public Options(long connectTimeout, TimeUnit connectTimeoutUnit,
                       long readTimeout, TimeUnit readTimeoutUnit,
                       boolean followRedirects) {
            super();
            this.connectTimeout = connectTimeout;
            this.connectTimeoutUnit = connectTimeoutUnit;
            this.readTimeout = readTimeout;
            this.readTimeoutUnit = readTimeoutUnit;
            this.followRedirects = followRedirects;
        }

        /**
         * Creates the new Options instance using the following defaults:
         * <ul>
         * <li>Connect Timeout: 10 seconds</li>
         * <li>Read Timeout: 60 seconds</li>
         * <li>Follow all 3xx redirects</li>
         * </ul>
         */
        public Options() {
            this(10, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true);
        }

        public int connectTimeoutMillis() {
            return (int) connectTimeoutUnit.toMillis(connectTimeout);
        }

        public int readTimeoutMillis() {
            return (int) readTimeoutUnit.toMillis(readTimeout);
        }

        public boolean isFollowRedirects() {
            return followRedirects;
        }

        public long connectTimeout() {
            return connectTimeout;
        }

        public TimeUnit connectTimeoutUnit() {
            return connectTimeoutUnit;
        }

        public long readTimeout() {
            return readTimeout;
        }

        public TimeUnit readTimeoutUnit() {
            return readTimeoutUnit;
        }

    }

}
