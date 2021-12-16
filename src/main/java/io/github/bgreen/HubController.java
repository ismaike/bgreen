package io.github.bgreen;

import io.github.model.MetricConfig;
import io.github.model.MetricParam;
import io.github.model.RegistryParam;
import io.github.model.Result;
import io.github.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

/**
 * @author maike
 * @date 2021年12月08日 10:34 下午
 */
@Slf4j
@RestController
public class HubController {

    @Resource
    private RestTemplate restTemplate;

    @PostMapping("upload")
    public Result<String> upload(HttpServletRequest request, @RequestBody MetricParam param) {
        System.out.println("remoteAddr" + request.getRemoteAddr());
        log.info("已收到上报数据: data={}", GsonUtil.toJson(param));
        return Result.success("已收到上报数据");
    }

    @PostMapping("registry")
    public Result<String> registry(HttpServletRequest request, @RequestBody RegistryParam param) {
        // TODO 存储
        return Result.success("注册成功");
    }

    @PostMapping("unReigstry")
    public Result<String> unReigstry(HttpServletRequest request, @RequestBody RegistryParam param) {
        // TODO 存储更新
        return Result.success("注销成功");
    }

    @PostMapping("config/{groupCode}/{appName}")
    public Result<Boolean> config(@PathVariable String groupCode, @PathVariable String appName, @RequestBody MetricConfig config) {
        // TODO 根据groupCode和appName查询服务器地址，用于定向推送配置

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> request =
                new HttpEntity<>(GsonUtil.toJson(config), headers);
        ParameterizedTypeReference<Result<Boolean>> resultRef = new ParameterizedTypeReference<Result<Boolean>>() {
        };
        ResponseEntity<Result<Boolean>> responseEntity = restTemplate.exchange("http://127.0.0.1:8113/pushConfig", HttpMethod.POST, request, resultRef);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            log.info("config push success!");
        } else {
            log.info("config push fail");
        }
        return Result.success(responseEntity.getBody().getData());
    }
}
