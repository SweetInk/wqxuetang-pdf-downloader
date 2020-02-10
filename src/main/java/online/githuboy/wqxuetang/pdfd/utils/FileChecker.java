package online.githuboy.wqxuetang.pdfd.utils;

import online.githuboy.wqxuetang.pdfd.Constants;

import java.util.function.Consumer;

/**
 * 文件检查
 *
 * @author suchu
 * @since 2020年2月10日
 */
public class FileChecker {
    static Consumer<Boolean> NO_OP = result -> {
    };

    /**
     * check file whether invalid
     *
     * @param size     file size
     * @param consumer result <code> <br/>true:file invalid,<br/>false:normal file</code>
     */
    public static void check(long size, Consumer<Boolean> consumer) {
        if (size <= Constants.IMG_INVALID_SIZE
                || size <= 8
                || size == Constants.IMG_LOADING_SIZE) {
            //无效图片
            consumer.accept(true);
        } else {
            consumer.accept(false);
        }
    }

    public static void check(long size) {
        check(size, NO_OP);
    }
}
