package online.githuboy.wqxuetang.pdfd;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import online.githuboy.wqxuetang.pdfd.api.ApiUtils;
import online.githuboy.wqxuetang.pdfd.pojo.BookMetaInfo;
import online.githuboy.wqxuetang.pdfd.pojo.Catalog;
import online.githuboy.wqxuetang.pdfd.utils.PDFUtils;
import online.githuboy.wqxuetang.pdfd.utils.ThreadPoolUtils;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
public class App {

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {

        Options options = new Options();
        options.addOption(Option.builder("b").hasArg(true).required().desc("The id of book").build());
        options.addOption(Option.builder("c").hasArg(true).required().desc("PHPSESSID Cookie value after successful login to wqxuetang").build());
        options.addOption(Option.builder("w").hasArg(true).required().desc("Use for store the Pdf file and The temp file").build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            String footer = "\nPlease report issues at https://github.com/SweetInk/wqxuetang-pdf-downloader";
            formatter.printHelp("java -jar pdfd.jar", "\n", options, footer, true);
            return;
        }

        String bookId = cmd.getOptionValue("b");
        String workDir = cmd.getOptionValue("w");
        String cookieValue = cmd.getOptionValue("c");

        CookieStore.COOKIE = "PHPSESSID=" + cookieValue;

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
        Map<Integer, Integer> pageMap = new HashMap<>();
        for (int i = 1; i <= pages; i++) {
            pageMap.put(i, i);
        }
        File tempSaveDir = new File(workDir, bookId);
        if (!tempSaveDir.exists())
            tempSaveDir.mkdirs();
        List<Integer> strings = FileUtil.listFileNames(workDir + "\\" + bookId).stream().map(
                fileName -> Integer.parseInt(fileName.substring(0, fileName.lastIndexOf('.')))
        ).collect(Collectors.toList());
        Collections.sort(strings);
        //之前成功下载的图片将会跳过
        List<Integer> failedImageList = pageMap.entrySet().stream().filter(entry -> !strings.contains(entry.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());

        if (failedImageList.size() <= 0) {
            log.info("All image downloaded");
            log.info("Ready for generate PDF");
            PDFUtils.gen(metaInfo, catalogs, workDir);
            log.info("All finished take :{}s", (System.currentTimeMillis() - start) / 1000);
        } else {
            ThreadPoolExecutor executor = ThreadPoolUtils.getExecutor();
            CountDownLatch latch = new CountDownLatch(failedImageList.size());
            log.info("Start download image");
            for (Integer page : failedImageList) {
                FetchBookImageTask task = new FetchBookImageTask(workDir, bookId, page, k, latch);
                executor.execute(task);
            }
            latch.await();
            long successCount = AppContext.getImageStatusMapping().entrySet().stream().filter(Map.Entry::getValue).count();
            if (successCount == failedImageList.size()) {
                log.info("All image downloaded");
                log.info("Ready for generate PDF");
                PDFUtils.gen(metaInfo, catalogs, workDir);
                log.info("All finished take :{}s", (System.currentTimeMillis() - start) / 1000);

            } else {
                AppContext.getImageStatusMapping().entrySet().stream().filter(entry -> !entry.getValue()).map(Map.Entry::getKey).forEach(pageNumber -> {
                    log.info("图片:{}下载失败", pageNumber);
                    FileUtil.del(new File(workDir + "\\" + bookId, pageNumber + ".jpg"));
                });
                log.info("请重新运行本程序下载");
            }
            executor.shutdown();
        }
    }


}
