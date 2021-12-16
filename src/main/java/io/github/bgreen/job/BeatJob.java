package io.github.bgreen.job;

/**
 * @author maike
 * @date 2021年12月16日 11:11 上午
 */

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class BeatJob {

    /**
     * 定时心跳检测
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void beat() {
        // TODO 轮询所有注册的客户端，如果连续3次心跳检测失败，则剔除
    }
}
