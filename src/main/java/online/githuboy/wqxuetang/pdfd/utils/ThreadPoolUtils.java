package online.githuboy.wqxuetang.pdfd.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池工具
 *
 * @author suchu
 * @since 2020年2月3日
 */
public class ThreadPoolUtils {

    private static int coreSize = Runtime.getRuntime().availableProcessors() * 2;
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(coreSize,
            coreSize,
            0,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());

    private ThreadPoolUtils() {
    }

    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }
}
