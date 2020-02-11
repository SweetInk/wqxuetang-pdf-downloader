package online.githuboy.wqxuetang.pdfd;

/**
 * 常量定义
 */
public class Constants {

    public static final String BOOK_CATE = "https://lib-nuanxin.wqxuetang.com/v1/book/catatree";

    public static final String BOOK_META = "https://lib-nuanxin.wqxuetang.com/v1/read/initread?bid={0}";

    public static final String BOOK_KEY = "https://lib-nuanxin.wqxuetang.com/v1/read/k?bid={0}";

    public static final String NVC = "https://lib-nuanxin.wqxuetang.com/page/nvc";
    /**
     * 0 -> bookId
     * 1 -> pageNum
     * 2 -> key
     */
    public static final String BOOK_IMG = "https://lib-nuanxin.wqxuetang.com/page/lmg/{0}/{1}";
    /**
     * JWT签名密钥
     */
    public static final String JWT_SECRET = "g0NnWdSE8qEjdMD8a1aq12qEYphwErKctvfd3IktWHWiOBpVsgkecur38aBRPn2w";
    /**
     * 通常图片内容为'加载中'，图标的大小将小于等于这个数字（观察的来的）
     */
    public static final int IMG_LOADING_SIZE = 10400;

    /**
     * 图片如果下载失败，图片内容固定5字节
     */
    public static final int IMG_INVALID_SIZE = 5;

    public static final int MAX_RETRY_COUNT = 2;

    public static final String JPG_SUFFIX = ".jpg";

    public static final String DEFAULT_CONFIG_FILE = "config.properties";
}
