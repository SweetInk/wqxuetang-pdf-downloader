package online.githuboy.wqxuetang.pdfd;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import online.githuboy.wqxuetang.pdfd.pojo.Config;
import online.githuboy.wqxuetang.pdfd.ui.WebContainer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AppContext
 *
 * @author suchu
 * @since 2020年2月4日
 */
@Slf4j
public class AppContext {

    private static ConcurrentHashMap<String, Boolean> imageStatusMapping = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, String> imgKCache = new ConcurrentHashMap<>();

    public static AtomicInteger counter = new AtomicInteger(0);

    public static ConcurrentHashMap<String, Boolean> getImageStatusMapping() {
        return imageStatusMapping;
    }

    public static String getBookKey(String bookId) {
        return imgKCache.get(bookId);
    }

    public static void setBookKey(String bookId, String key) {
        imgKCache.put(bookId, key);
    }


    @Setter
    @Getter
    public static WebContainer webContainer;

    @Setter
    @Getter
    public static Config config = new Config();

}
