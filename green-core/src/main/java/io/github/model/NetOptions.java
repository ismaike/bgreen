package io.github.model;

import lombok.Builder;

import java.util.concurrent.TimeUnit;

/**
 * @author maike
 * @date 2021年12月15日 5:14 下午
 */
@Builder
public class NetOptions {

    private long connectTimeout;
    private TimeUnit connectTimeoutUnit;
    private long readTimeout;
    private TimeUnit readTimeoutUnit;

    public NetOptions(long connectTimeout, TimeUnit connectTimeoutUnit,
                      long readTimeout, TimeUnit readTimeoutUnit) {
        super();
        this.connectTimeout = connectTimeout;
        this.connectTimeoutUnit = connectTimeoutUnit;
        this.readTimeout = readTimeout;
        this.readTimeoutUnit = readTimeoutUnit;
    }

    public NetOptions() {
        this(10, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);
    }

    public int connectTimeoutMillis() {
        return (int) connectTimeoutUnit.toMillis(connectTimeout);
    }

    public int readTimeoutMillis() {
        return (int) readTimeoutUnit.toMillis(readTimeout);
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
