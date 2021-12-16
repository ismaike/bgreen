package io.github;

import io.github.remoting.server.EmbedHttpServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * @author maike
 * @date 2021年12月08日 7:30 下午
 */
@Slf4j
public class BgreenBootstrap implements SmartInitializingSingleton, DisposableBean {

    private EmbedHttpServer embedHttpServer;

    @Override
    public void afterSingletonsInstantiated() {
        embedHttpServer = new EmbedHttpServer();
        embedHttpServer.start(8113);
    }

    @Override
    public void destroy() throws Exception {
        try {
            embedHttpServer.stop();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
