package io.github.model;

import lombok.Data;

import java.util.Properties;

/**
 * @author maike
 * @date 2021年12月08日 8:26 下午
 */
@Data
public class MetricParam {

    private Integer metricType;
    private Properties properties;

    public void put(String metricKey, String metricValue) {
        properties.put(metricKey, metricValue);
    }

    public String getMetric(String metricKey) {
        return properties.getProperty(metricKey);
    }
}
