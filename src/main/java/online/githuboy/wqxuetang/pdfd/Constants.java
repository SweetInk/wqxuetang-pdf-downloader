package online.githuboy.wqxuetang.pdfd;

/**
 * 常量定义
 */
public class Constants {

    public static final String BOOK_CATE = "https://lib-nuanxin.wqxuetang.com/v1/book/catatree?bid={0}";

    public static final String BOOK_META = "https://lib-nuanxin.wqxuetang.com/v1/read/initread?bid={0}";

    public static final String BOOK_KEY = "https://lib-nuanxin.wqxuetang.com/v1/read/k?bid={0}";

    /**
     * 0 -> bookId
     * 1 -> pageNum
     * 2 -> key
     */
    public static final String BOOK_IMG = "https://lib-sig.wqxuetang.com/page/img/{0}/{1}?k={2}";
    /**
     * JWT签名密钥
     */
    public static final String JWT_SECRET = "g0NnWdSE8qEjdMD8a1aq12qEYphwErKctvfd3IktWHWiOBpVsgkecur38aBRPn2w";
    /**
     * 通常图片无效或者内容为'加载中'，图标的大小将小于等于这个数字（观察的来的）
     */
    public static final int IMG_INVALID_SIZE = 10400;
}
