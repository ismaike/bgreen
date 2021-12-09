package io.github.bgreen;

import io.github.model.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author maike
 * @date 2021年12月08日 10:34 下午
 */
@RestController
public class HubController {

    @PostMapping("upload")
    public Result<String> upload() {
        return Result.success("已收到上报数据");
    }
}
