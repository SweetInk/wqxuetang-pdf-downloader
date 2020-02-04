package online.githuboy.wqxuetang.pdfd.api;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import online.githuboy.wqxuetang.pdfd.Constants;
import online.githuboy.wqxuetang.pdfd.pojo.BookMetaInfo;
import online.githuboy.wqxuetang.pdfd.pojo.Catalog;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;

/**
 * API 请求工具
 *
 * @author suchu
 * @since 2020年2月3日
 */
@Slf4j
public class ApiUtils {
    /**
     * 获取书籍元数据
     *
     * @param bid 书籍id
     * @return BookMetaInfo
     */
    public static BookMetaInfo getBookMetaInfo(String bid) {
        String url = MessageFormat.format(Constants.BOOK_META, bid);
        return JSONUtil.toBean(getAndCheck(url).getJSONObject("data"), BookMetaInfo.class);
    }

    /**
     * 获取书籍目录信息
     *
     * @param bid 书籍id
     * @return 书籍目录列表
     */
    public static List<Catalog> getBookCatalog(String bid) {
        String url = MessageFormat.format(Constants.BOOK_CATE, bid);
        return JSONUtil.toList(getAndCheck(url).getJSONArray("data"), Catalog.class);
    }

    /**
     * 获取书籍图片验证Key
     *
     * @param bid 书籍id
     * @return key
     */
    public static String getBookKey(String bid) {
        String url = MessageFormat.format(Constants.BOOK_KEY, bid);
        JSONObject result = getAndCheck(url);
        return result.getJSONObject("data").toString();
    }

    /**
     * GET方式请求API,并验证
     *
     * @param url url地址
     * @return 响应体JSONObject
     */
    private static JSONObject getAndCheck(String url) {

        HttpResponse response = HttpRequest.get(url)
                .charset(CharsetUtil.CHARSET_UTF_8)
                .executeAsync();
        if (!response.isOk()) {
            throw new HttpException("Request url [{}] ,Server response error with status code: [{}]", url, response.getStatus());
        } else {
            String test = new String(response.bodyBytes(), StandardCharsets.UTF_8);
            JSONObject data = JSONUtil.parseObj(test);
            int errCode = data.getInt("errcode");
            if (errCode != 0) {
                String errMsg = data.getStr("errmsg");
                throw new RuntimeException("Request url:" + url + " failed,errorMessage:" + errMsg);
            }
            return data;
        }
    }

    public static void main(String[] args) {
        String bookId = "2175744";
        List<Catalog> bookCatalog = getBookCatalog(bookId);
        log.info("catalogs:{}", bookCatalog);
        log.info("book key:{}", getBookKey(bookId));
        log.info("meta info:{}", getBookMetaInfo(bookId));
    }
}
