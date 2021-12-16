package io.github.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author maike
 * @date 2021年12月16日 11:39 上午
 */
public class VersionUtil {

    private static AtomicInteger version = new AtomicInteger(0);

    public static int createNewVersion(){
        return version.incrementAndGet();
    }
}
