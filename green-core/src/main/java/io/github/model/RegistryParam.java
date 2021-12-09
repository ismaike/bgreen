package io.github.model;

import lombok.Data;

/**
 * @author maike
 * @date 2021年12月07日 10:56 下午
 */
@Data
public class RegistryParam {

    private String groupCode;
    private String appName;
    private String address;

    public RegistryParam(String groupCode, String appName, String address) {
        this.groupCode = groupCode;
        this.appName = appName;
        this.address = address;
    }
}
