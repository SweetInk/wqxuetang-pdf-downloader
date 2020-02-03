package online.githuboy.wqxuetang.pdfd;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import online.githuboy.wqxuetang.pdfd.api.ApiUtils;
import online.githuboy.wqxuetang.pdfd.pojo.BookMetaInfo;
import online.githuboy.wqxuetang.pdfd.pojo.Catalog;
import online.githuboy.wqxuetang.pdfd.utils.JwtUtils;
import online.githuboy.wqxuetang.pdfd.utils.PDFUtils;
import online.githuboy.wqxuetang.pdfd.utils.ThreadPoolUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class App {

    static String workDir = "D:\\Temp";

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 2) {
            usage();
            return;
        }

        String bookId = null;
        String workDir = null;
        for (String arg : args) {
            if (StrUtil.isBlank(arg)) continue;
            if (null == bookId)
                bookId = arg;
            else {
                workDir = arg;
                break;
            }
        }
        if (null == bookId || null == workDir) {
            usage();
            return;
        }
        log.info("Fetch book info bookId:{} ", bookId);
        long start = System.currentTimeMillis();
        BookMetaInfo metaInfo = ApiUtils.getBookMetaInfo(bookId);
        // Expire at 5 minutes later;
        String k = ApiUtils.getBookKey(bookId);
        List<Catalog> catalogs = ApiUtils.getBookCatalog(bookId);
        log.info("meta:{}", metaInfo);
        log.info("k:{}", k);
        log.info("cate:{}", catalogs);
        int pages = metaInfo.getPages();
        ThreadPoolExecutor executor = ThreadPoolUtils.getExecutor();
        CountDownLatch latch = new CountDownLatch(metaInfo.getPages());
        log.info("Start download image");
        for (int page = 1; page <= pages; page++) {
            String key = JwtUtils.getJwt(bookId, String.valueOf(page), k);
            FetchBookImageTask task = new FetchBookImageTask(workDir, bookId, page, key, latch);
            executor.execute(task);
        }
        latch.await();
        log.info("All image downloaded");
        log.info("Ready for generate PDF");
        PDFUtils.gen(metaInfo, catalogs, workDir);
        log.info("All finished take :{}s", (System.currentTimeMillis() - start) / 1000);
        executor.shutdown();
    }

    private static void usage() {
        System.out.println("usage: pdfd <bookId> <workDir>\n");
        System.out.println("\tbookId - The id of book.\n");
        System.out.println("\tworkDir - Use for store the Pdf file and tempFile.\n");
    }
}
