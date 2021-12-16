package io.github;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author maike
 * @date 2021年12月07日 11:02 下午
 */
@Data
@ConfigurationProperties(prefix = "bgreen")
public class BgreenProperties {

    /**
     * 服务器地址, 格式: `host:port;host:port`.
     */
    private List<String> bgreenServer;
    private String groupCode;
    private String appName;
    private boolean enabled;

    private NetOptions netOptions;
    private HttpRequest httpRequest;
    private SqlExecutor sqlExecutor;
    private Exception exception;

    @Data
    public static class NetOptions {
        private long connectTimeout = 3000;
        private TimeUnit connectTimeoutUnit = TimeUnit.MILLISECONDS;
        private long readTimeout = 3000;
        private TimeUnit readTimeoutUnit = TimeUnit.MILLISECONDS;
    }

    @Data
    public static class HttpRequest {
        /**
         * 请求超时阈值
         */
        private int requestTimeoutThreshold = 1000;
    }

    @Data
    public static class SqlExecutor {
        /**
         * sql执行超时阈值
         */
        private int sqlTimeoutThreshold = 3000;
    }

    @Data
    public static class Exception {
        /**
         * 异常拦截的包路径
         */
        private String exceptionInterceptPackagePath;

        /**
         * 排除的异常类, 全限定类名
         */
        private Set<String> exceptionExcludedSet;

        /**
         * 异常拦截堆栈长度
         */
        private int exceptionStackLength = 5;
    }

}
