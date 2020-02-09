package online.githuboy.wqxuetang.pdfd.api;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.*;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import online.githuboy.wqxuetang.pdfd.AppContext;
import online.githuboy.wqxuetang.pdfd.Constants;
import online.githuboy.wqxuetang.pdfd.CookieStore;
import online.githuboy.wqxuetang.pdfd.pojo.BookMetaInfo;
import online.githuboy.wqxuetang.pdfd.pojo.Catalog;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        JSONObject data = getAndCheck(url).getJSONObject("data");
        BookMetaInfo metaInfo = JSONUtil.toBean(getAndCheck(url).getJSONObject("data"), BookMetaInfo.class);
        JSONArray volume_list = data.getJSONArray("volume_list");
        if (null != volume_list && volume_list.size() > 0) {
            metaInfo.setVolumeList(JSONUtil.toList(volume_list, BookMetaInfo.class));
        } else {
            metaInfo.setVolumeList(Collections.emptyList());
        }
        return metaInfo;
    }

    /**
     * 获取书籍目录信息
     *
     * @param bid 书籍id
     * @return 书籍目录列表
     */
    public static List<Catalog> getBookCatalog(String bid, String volumeNo) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("bid", bid);
        if (null != volumeNo) {
            paramMap.put("volume_no", volumeNo);
        }
        String params = HttpUtil.toParams(paramMap);
        String url = Constants.BOOK_CATE + '?' + params;
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

    public static String requestNvc(String bookId, String pageNumber, String volumeNumber) {
        Map<String, String> param = new HashMap<>();
        String referUrl = "https://lib-nuanxin.wqxuetang.com/read/pdf/" + bookId;
        param.put("bid", bookId);
        param.put("pnum", pageNumber);
        param.put("volume_no", StrUtil.isBlank(volumeNumber) ? "" : volumeNumber);
        String nvc = AppContext.getWebContainer().getNVC();
        param.put("nvc", nvc);
        String body = HttpRequest.get(Constants.NVC + "?" + HttpUtil.toParams(param))
                .cookie(CookieStore.COOKIE)
                .header(Header.HOST, "lib-nuanxin.wqxuetang.com")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("User", "bapkg/com.bookask.wqxuetang baver/1.1.1")
                .header(Header.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4041.0 Safari/537.36 Edg/81.0.410.1")
                .execute().body();
        log.info("NVC Response:{}", body);
        return null;
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
                .cookie(CookieStore.COOKIE)
                .executeAsync();
        if (!response.isOk()) {
            throw new HttpException("Request url [{}] ,Server response error with status code: [{}]", url, response.getStatus());
        } else {
            String test = new String(response.bodyBytes(), StandardCharsets.UTF_8);
            JSONObject data;
            try {
                data = JSONUtil.parseObj(test);
            } catch (Exception e) {
                throw new RuntimeException("服务器开启了Ali滑动验证，请求失败:" + test);
            }
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
        List<Catalog> bookCatalog = getBookCatalog(bookId, null);
        log.info("catalogs:{}", bookCatalog);
        log.info("book key:{}", getBookKey(bookId));
        log.info("meta info:{}", getBookMetaInfo(bookId));
    }
}
