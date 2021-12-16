package io.github.model;

import io.github.util.VersionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * @author maike
 * @date 2021年12月15日 5:15 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricConfig {

    private static volatile MetricConfig cache = new MetricConfig();

    public static MetricConfig getCache() {
        return cache;
    }

    public static void setCache(MetricConfig cache) {
        cache.setVersion(VersionUtil.createNewVersion());
        MetricConfig.cache = cache;
    }

    private int version = 0;

    /**
     * 请求超时阈值
     */
    private int requestTimeoutThreshold = 1000;
    /**
     * sql执行超时阈值
     */
    private int sqlTimeoutThreshold = 3000;
    /**
     * 异常拦截的包路径
     */
    private String exceptionInterceptPackagePath;
    /**
     * 排除的异常类, 全限定类名
     */
    private Set<String> exceptionExcludedSet = new HashSet<>();
    /**
     * 异常拦截堆栈长度
     */
    private int exceptionStackLength = 5;
}
