package io.github.model;

import lombok.Data;

import java.util.Properties;

/**
 * @author maike
 * @date 2021年12月08日 8:26 下午
 */
@Data
public class MetricParam {

    private int metricType;
    private String groupCode;
    private String appName;
    private Properties properties;

    public MetricParam() {
        properties = new Properties();
    }

    public MetricParam(int metricType) {
        this.metricType = metricType;
        properties = new Properties();
    }

    public MetricParam(int metricType, String groupCode, String appName) {
        this.metricType = metricType;
        this.groupCode = groupCode;
        this.appName = appName;
        properties = new Properties();
    }

    public void put(String metricKey, String metricValue) {
        properties.put(metricKey, metricValue);
    }

    public String getMetric(String metricKey) {
        return properties.getProperty(metricKey);
    }
}
