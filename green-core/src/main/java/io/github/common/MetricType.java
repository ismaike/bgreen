package io.github.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author maike
 * @date 2021年12月15日 3:52 下午
 */
@Getter
@AllArgsConstructor
public enum MetricType {

    /**
     * Http请求拦截, GET,POST,...
     */
    HTTP_REQUEST(0, "Http请求拦截"),
    /**
     * Sql执行超时拦截
     */
    SQL_EXECUTOR(1, "SQL执行拦截"),
    /**
     * 异常捕获
     */
    EXCEPTION_HANDLER(2, "异常捕获");

    private int type;
    private String desc;

    public static MetricType getByType(int type) {
        return Arrays.stream(values()).filter(e -> e.getType() == type).findFirst().orElse(null);
    }
}
