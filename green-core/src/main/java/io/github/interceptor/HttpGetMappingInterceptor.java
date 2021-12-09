package io.github.interceptor;

import io.github.Intercept;
import io.github.common.Metric;
import io.github.model.MetricParam;
import io.github.remoting.api.ServerApi;
import io.github.remoting.client.OkHttpClient;
import io.github.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author maike
 * @date 2021年12月09日 2:46 下午
 */
@Slf4j
@Aspect
public class HttpGetMappingInterceptor implements Intercept {

    private static final String URL_SEPARATOR = "/";

    @Around("@annotation(org.springframework.web.bind.annotation.GetMapping)")
    public Object invoke(ProceedingJoinPoint invocation) throws Throwable {
        // 记录当前时间
        long start = System.currentTimeMillis();
        MetricParam param = new MetricParam();
        param.put(Metric.REQUEST_METHOD, "GET");
        for (Object arg : invocation.getArgs()) {
            if (arg instanceof HttpServletRequest) {
                HttpServletRequest req = (HttpServletRequest) arg;
                param.put(Metric.REQUEST_URI, req.getRequestURI());
                param.put(Metric.REQUEST_COOKIES, GsonUtil.toJson(req.getCookies()));
            }
            // 排除一些已知的不支持序列化的对象
            if (arg instanceof ServletRequest || arg instanceof ServletResponse || arg instanceof MultipartFile) {
                continue;
            }
            // 为确保序列化失败影响程序正常运行，这里 catch 一下异常
            StringBuilder requestParam = new StringBuilder();
            try {
                requestParam.append(arg.getClass().getSimpleName()).append(":").append(GsonUtil.toJson(arg)).append("; ");
            } catch (Exception e) {
                log.info("gson序列化请求参数出现异常！", e);
            }
            param.put(Metric.REQUEST_PARAM, requestParam.toString());
        }
        if (param.getMetric(Metric.REQUEST_URI) == null) {
            StringBuilder url = new StringBuilder();
            Signature signature = invocation.getSignature();
            Method targetMethod = ((MethodSignature) signature).getMethod();
            RequestMapping requestMappingAnnotation = invocation.getTarget().getClass().getAnnotation(RequestMapping.class);
            if (requestMappingAnnotation != null) {
                url = new StringBuilder(wrapPrefixPathSeparator(Arrays.stream(requestMappingAnnotation.value()).findFirst().orElse("")));
            }
            GetMapping getMapping = targetMethod.getAnnotation(GetMapping.class);
            url.append(wrapPrefixPathSeparator(Arrays.stream(getMapping.value()).findFirst().orElse("")));
            param.put(Metric.REQUEST_URI, url.toString());
        }

        Object result;
        try {
            // 执行请求
            result = invocation.proceed();
        } catch (Exception e) {
            throw e;
        }
        // 计算请求耗时时长
        long costTime = System.currentTimeMillis() - start;
        param.put("costTime", String.valueOf(costTime));

        send(param);

        return result;
    }

    @Override
    public ServerApi api() {
        return new OkHttpClient("http://127.0.0.1:8080/upload");
    }

    private String wrapPrefixPathSeparator(String path) {
        if (path.startsWith(URL_SEPARATOR)) {
            return path;
        }
        return URL_SEPARATOR + path;
    }
}