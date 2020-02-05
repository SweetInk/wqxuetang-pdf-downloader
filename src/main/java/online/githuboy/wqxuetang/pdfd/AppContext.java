package online.githuboy.wqxuetang.pdfd;

import java.util.concurrent.ConcurrentHashMap;

/**
 * AppContext
 *
 * @author suchu
 * @since 2020年2月4日
 */
public class AppContext {
    private static ConcurrentHashMap<String, Boolean> imageStatusMapping = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> imgKCache = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Boolean> getImageStatusMapping() {
        return imageStatusMapping;
    }

    public static String getBookKey(String bookId) {
        return imgKCache.get(bookId);
    }

    public static void setBookKey(String bookId, String key) {
        imgKCache.put(bookId, key);
    }
}
