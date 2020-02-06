package online.githuboy.wqxuetang.pdfd.utils;

import java.util.Optional;
import java.util.concurrent.*;

/**
 * 线程池工具
 *
 * @author suchu
 * @since 2020年2月3日
 */
public class ThreadPoolUtils {

    private static ThreadPoolExecutor executor;
    private static ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2);

    public static void init(Integer coreSize) {
        int actualSize = Optional.ofNullable(coreSize).orElse(1);
        executor = new ThreadPoolExecutor(actualSize,
                actualSize,
                0,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
    }


    private ThreadPoolUtils() {
    }

    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }
}
