package io.github.thread;

import io.github.common.Metric;
import io.github.common.MetricType;
import io.github.model.MetricConfig;
import io.github.model.MetricParam;
import io.github.model.NetOptions;
import io.github.model.Result;
import io.github.remoting.client.BgreenClientApi;
import io.github.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author maike
 * @date 2021年12月15日 3:33 下午
 */
@Slf4j
public class ReportThread {

    private static AtomicInteger threadNo = new AtomicInteger(0);
    private static AtomicInteger rejectTaskNum = new AtomicInteger(0);
    private static final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(),
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1000),
            runnable -> {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.setName("bgreen, report thread pool, thread_No_" + threadNo.incrementAndGet());
                return thread;
            },
            (runnable, executor) -> {
                log.error("bgreen, no more thread resources to handle too many tasks, reject num = {}", rejectTaskNum.incrementAndGet());
            });

    public static Result<String> report(MetricParam param, NetOptions options, MetricConfig config) {
        switch (MetricType.getByType(param.getMetricType())) {
            case HTTP_REQUEST:
                if (Integer.valueOf(param.getMetric(Metric.REQUEST_COST_TIME)).compareTo(config.getRequestTimeoutThreshold()) <= 0) {
                    break;
                }
                return doReport(param, options);
            case SQL_EXECUTOR:
                if (Integer.valueOf(param.getMetric(Metric.SQL_COST_TIME)).compareTo(config.getSqlTimeoutThreshold()) <= 0) {
                    break;
                }
                return doReport(param, options);
            case EXCEPTION_HANDLER:
                if (config.getExceptionExcludedSet().contains(param.getMetric(Metric.EXCEPTION_CLASS))) {
                    break;
                }
                return doReport(param, options);
            default:
                break;
        }
        return Result.fail("metric type is not support!");
    }

    public static Result<String> doReport(MetricParam param, NetOptions options) {
        FutureTask<Result<String>> reportTask = new FutureTask<>(() -> BgreenClientApi.reportMetrics(GsonUtil.toJson(param), options));
        poolExecutor.submit(reportTask);
        try {
            return reportTask.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            reportTask.cancel(true);
        }
        return Result.fail("report timeout!");
    }
}
