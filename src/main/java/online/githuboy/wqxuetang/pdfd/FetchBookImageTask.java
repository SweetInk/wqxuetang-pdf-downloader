package online.githuboy.wqxuetang.pdfd;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import javafx.application.Platform;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import online.githuboy.wqxuetang.pdfd.utils.JwtUtils;
import online.githuboy.wqxuetang.pdfd.utils.ThreadPoolUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 获取书籍图片书籍Task
 *
 * @author suchu
 * @since 2020年2月3日
 */
@Slf4j
public class FetchBookImageTask implements Runnable {
    private String bookId;
    private int pageNumber;
    private CountDownLatch latch;
    private File baseDir;
    private AtomicBoolean nvcResult = new AtomicBoolean(true);
    /**
     * 分区号
     */
    @Setter
    private String volumeNumber;
    private AtomicInteger retryCount = new AtomicInteger(0);

    public FetchBookImageTask(String imageTempDir, String bookId, int pageNumber, CountDownLatch latch) {
        this.bookId = bookId;
        this.pageNumber = pageNumber;
        this.latch = latch;
        this.baseDir = FileUtil.file(imageTempDir);
        if (!this.baseDir.exists()) {
            this.baseDir.mkdirs();
        }
    }

    private void requestNvc(CountDownLatch $latch) {
        Platform.runLater(() -> {
            Map<String, String> param = new HashMap<>();
            String referUrl = "https://lib-nuanxin.wqxuetang.com/read/pdf/" + bookId;
            param.put("bid", this.bookId);
            param.put("pnum", this.pageNumber + "");
            param.put("volume_no", StrUtil.isBlank(volumeNumber) ? "" : volumeNumber);
            try {
                String nvc = AppContext.getWebContainer().getNVC();
                param.put("nvc", nvc);
                HttpResponse response = HttpRequest.get(Constants.NVC + "?" + HttpUtil.toParams(param))
                        .cookie(CookieStore.COOKIE)
                        .header(Header.HOST, "lib-nuanxin.wqxuetang.com")
                        .header("Sec-Fetch-Dest", "empty")
                        .header("Sec-Fetch-Mode", "cors")
                        .header(Header.REFERER, referUrl)
                        .header("User", "bapkg/com.bookask.wqxuetang baver/1.1.1")
                        .header(Header.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4041.0 Safari/537.36 Edg/81.0.410.1")
                        .execute();
                String body = response.body();
                JSONObject object = JSONUtil.parseObj(body);
                int code = object.getInt("errcode");
                String msg = object.getStr("errmsg");
                if (code != 0) {
                    log.error("请求NVC出错 bookId:{},pageNumber:{},volumeNumber:{}, code:{},msg:{}", bookId, pageNumber, volumeNumber, code, msg);
                } else {
                    JSONObject data = object.getJSONObject("data");
                    String bizCode = data.getStr("BizCode");
                    if (!"200".equals(bizCode)) {
                        nvcResult.set(false);
                        log.info("NVC验证失败 bookId:{},pageNumber:{},volumeNumber:{},result:{}", bookId, pageNumber, volumeNumber, data.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                nvcResult.set(false);
            }
            $latch.countDown();
        });
    }

    @Override
    public void run() {
        File outFile = null;
        try {
            if (AppContext.counter.get() >= AppContext.getConfig().getMaxRequestThreshold()) {
                log.info("休眠{}秒再试", AppContext.getConfig().getWaitingSeconds());
                TimeUnit.SECONDS.sleep(AppContext.getConfig().getWaitingSeconds());
                log.info("任务继续执行");
                AppContext.counter.set(0);
            }
            String key = JwtUtils.getJwt(bookId, String.valueOf(this.pageNumber), AppContext.getBookKey(bookId));
            long start = System.currentTimeMillis();
            String url = MessageFormat.format(Constants.BOOK_IMG, this.bookId, this.pageNumber);
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("k", key);
            if (null != volumeNumber) {
                paramMap.put("v", volumeNumber);
            }
            url = url + '?' + HttpUtil.toParams(paramMap);
            String referUrl = "https://lib-nuanxin.wqxuetang.com/read/pdf/" + bookId;
            AppContext.counter.incrementAndGet();
            CountDownLatch $latch = new CountDownLatch(1);
            requestNvc($latch);
            $latch.await();
            if (!nvcResult.get()) {
                retry();
            }
            HttpResponse response = HttpRequest.get(url)
                    .header(cn.hutool.http.Header.REFERER, referUrl)
                    .cookie(CookieStore.COOKIE)
                    .header(Header.HOST, "lib-nuanxin.wqxuetang.com")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("User", "bapkg/com.bookask.wqxuetang baver/1.1.1")
                    .header(Header.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4041.0 Safari/537.36 Edg/81.0.410.1")
                    .timeout(150000)
                    .executeAsync();
            if (!response.isOk()) {
                log.error("Get page:{} img failed,Server response error with status code:{}", this.pageNumber, response.getStatus());
                log.info("休眠5s");
                Thread.sleep(5000);
                retry();
            } else {
                outFile = FileUtil.file(baseDir, this.pageNumber + Constants.JPG_SUFFIX);
                long size = response.writeBody(outFile, null);
                if (size <= Constants.IMG_INVALID_SIZE
                        || size == Constants.IMG_LOADING_SIZE) {
                    log.info("Page:{} 下载的图片无效，大小为:{} byte", this.pageNumber, size);
                    FileUtil.del(outFile);
                    retry();
                } else {
                    log.info("page:{}下载完成,耗时:{}s", this.pageNumber, (System.currentTimeMillis() - start) / 1000.f);
                    AppContext.getImageStatusMapping().put(String.valueOf(pageNumber), true);
                    latch.countDown();
                }
            }
        } catch (Exception e) {
            if (null != outFile)
                FileUtil.del(outFile);
            log.error("图片:{} 下载异常:{}", pageNumber, e.getMessage());
            retry();
        } finally {
            try {
                Thread.sleep(AppContext.getConfig().getDefaultSleepTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void retry() {
        int count = retryCount.getAndIncrement();
        if (count < Constants.MAX_RETRY_COUNT) {
            log.info("page:{} 图片获取进行第:{} 次重试", pageNumber, (count + 1));
            try {
                Thread.sleep(count * 700 + 500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ThreadPoolUtils.getExecutor().submit(this);
        } else {
            log.warn("page:{} 图片获取进行第:{} 次重试，依旧失败,放弃获取", pageNumber, count);
            AppContext.getImageStatusMapping().put(String.valueOf(pageNumber), false);
            latch.countDown();
        }

    }
}
