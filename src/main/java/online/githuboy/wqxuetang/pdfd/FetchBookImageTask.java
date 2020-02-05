package online.githuboy.wqxuetang.pdfd;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import online.githuboy.wqxuetang.pdfd.utils.JwtUtils;
import online.githuboy.wqxuetang.pdfd.utils.ThreadPoolUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 获取书籍图片书籍Task
 *
 * @author suchu
 * @since 2020年2月3日
 */
@Slf4j

public class FetchBookImageTask implements Runnable {
    private final static int maxRetryCount = 10;
    private String bookId;
    private int pageNumber;
    private CountDownLatch latch;
    private File baseDir;

    private AtomicInteger retryCount = new AtomicInteger(0);

    public FetchBookImageTask(String workDir, String bookId, int pageNumber, CountDownLatch latch) {
        this.bookId = bookId;
        this.pageNumber = pageNumber;
        this.latch = latch;
        this.baseDir = new File(workDir, bookId);
        if (!this.baseDir.exists()) {
            this.baseDir.mkdirs();
        }
    }

    @Override
    public void run() {
        File outFile = null;

        try {
            String key = JwtUtils.getJwt(bookId, String.valueOf(this.pageNumber), AppContext.getBookKey(bookId));
            long start = System.currentTimeMillis();
            HttpResponse response = HttpRequest.get(MessageFormat.format(Constants.BOOK_IMG, this.bookId, this.pageNumber, key))
                    .header(cn.hutool.http.Header.REFERER, "https://lib-nuanxin.wqxuetang.com/read/pdf/" + bookId)
                    .cookie(CookieStore.COOKIE)
                    .timeout(150000)
                    .executeAsync();
            if (!response.isOk()) {
                log.error("Get page:{} img failed,Server response error with status code:{}", this.pageNumber, response.getStatus());
                retry();
            } else {
                outFile = FileUtil.file(new File(baseDir, this.pageNumber + ".jpg"));
                long size = response.writeBody(outFile, null);
                if (size <= Constants.IMG_INVALID_SIZE
                        || size == Constants.IMG_LOADING_SIZE) {
                    log.info("图片错误:{}", this.pageNumber);
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
        }
    }

    private void retry() {
        int count = retryCount.getAndIncrement();
        if (count < maxRetryCount) {
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
