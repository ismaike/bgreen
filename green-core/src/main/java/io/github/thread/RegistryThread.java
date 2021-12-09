package io.github.thread;

import io.github.ExecutorProperties;
import io.github.common.RegistryConfig;
import io.github.model.RegistryParam;
import io.github.model.Result;
import io.github.remoting.api.ServerApi;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author maike
 * @date 2021年12月07日 11:00 下午
 */
@Slf4j
public class RegistryThread {

    private static RegistryThread instance = new RegistryThread();
    private Thread registryThread;
    private volatile boolean toStop = false;

    public static RegistryThread getInstance() {
        return instance;
    }

    public void start(final String appName, final String address) {

        // valid
        if (appName == null || appName.trim().length() == 0) {
            log.warn("registry config fail, appName is null.");
            return;
        }
        if (ExecutorProperties.getAdminBizList() == null) {
            log.warn("registry config fail, adminAddresses is null.");
            return;
        }

        registryThread = new Thread(() -> {

            // registry
            while (!toStop) {
                try {
                    RegistryParam registryParam = new RegistryParam(ExecutorProperties.getGroupCode(), appName, address);
                    for (ServerApi remoteApi : ExecutorProperties.getAdminBizList()) {
                        try {
                            Result<String> registryResult = remoteApi.registry(registryParam);
                            if (registryResult != null && Result.success().getCode() == registryResult.getCode()) {
                                registryResult = Result.success();
                                log.debug("registry success, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                                break;
                            } else {
                                log.info("registry fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                            }
                        } catch (Exception e) {
                            log.info("registry error, registryParam:{}", registryParam, e);
                        }

                    }
                } catch (Exception e) {
                    if (!toStop) {
                        log.error(e.getMessage(), e);
                    }

                }

                try {
                    if (!toStop) {
                        TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                    }
                } catch (InterruptedException e) {
                    if (!toStop) {
                        log.warn("registry thread interrupted, error msg:{}", e.getMessage());
                    }
                }
            }

            // registry remove
            try {
                RegistryParam registryParam = new RegistryParam(ExecutorProperties.getGroupCode(), appName, address);
                for (ServerApi remoteApi : ExecutorProperties.getAdminBizList()) {
                    try {
                        Result<String> registryResult = remoteApi.registryRemove(registryParam);
                        if (registryResult != null && Result.success().getCode() == registryResult.getCode()) {
                            registryResult = Result.success();
                            log.info("registry-remove success, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                            break;
                        } else {
                            log.info("registry-remove fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                        }
                    } catch (Exception e) {
                        if (!toStop) {
                            log.info("registry-remove error, registryParam:{}", registryParam, e);
                        }

                    }

                }
            } catch (Exception e) {
                if (!toStop) {
                    log.error(e.getMessage(), e);
                }
            }
            log.info("registry thread destroy.");

        });
        registryThread.setDaemon(true);
        registryThread.setName("registryThread");
        registryThread.start();
    }

    public void toStop() {
        toStop = true;

        // interrupt and wait
        if (registryThread != null) {
            registryThread.interrupt();
            try {
                registryThread.join();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }

    }


}
