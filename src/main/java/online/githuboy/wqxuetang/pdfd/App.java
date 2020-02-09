package online.githuboy.wqxuetang.pdfd;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import online.githuboy.wqxuetang.pdfd.api.ApiUtils;
import online.githuboy.wqxuetang.pdfd.pojo.BookMetaInfo;
import online.githuboy.wqxuetang.pdfd.pojo.Catalog;
import online.githuboy.wqxuetang.pdfd.pojo.Config;
import online.githuboy.wqxuetang.pdfd.utils.Cli;
import online.githuboy.wqxuetang.pdfd.utils.PDFUtils;
import online.githuboy.wqxuetang.pdfd.utils.ThreadPoolUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class App {

    public static void main(String[] args) throws IOException, InterruptedException {
        App app = new App();
        Cli cli = new Cli();
        cli.get(args);
        app.start();
    }

    public void start() {
        Config config = AppContext.getConfig();
        String bookId = config.getBookId();
        log.info("Fetch book info bookId:{} ", bookId);
        try {
            BookMetaInfo metaInfo = ApiUtils.getBookMetaInfo(bookId);
            // Expire at 5 minutes later;
            String bookKey = ApiUtils.getBookKey(bookId);
            AppContext.setBookKey(bookId, bookKey);
            configKeyFetchTask(bookId);
            log.info("meta:{}", metaInfo);
            log.info("k:{}", bookKey);
            ThreadPoolUtils.init(config.getThreadCount());
            if (metaInfo.getVolumeList().size() > 0) {
                handleMultipleVolume(metaInfo.getVolumeList());
            } else {
                handleSingleVolume(metaInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        ThreadPoolUtils.getScheduledExecutorService().shutdownNow();
        ThreadPoolUtils.getExecutor().shutdown();
        System.exit(1);
    }


    private void configKeyFetchTask(String bookId) {
        ThreadPoolUtils.getScheduledExecutorService().schedule(() -> {
            log.info("清理book:{} key", bookId);
            try {
                String bookKey = ApiUtils.getBookKey(bookId);
                AppContext.setBookKey(bookId, bookKey);
                log.info("更新book key成功:{}", bookKey);
            } catch (Exception e) {
                log.error("Cookie已失效，或者服务不可用 ,请退出程序重试,errorMessage:{}", e.getMessage());
            }
        }, 4, TimeUnit.MINUTES);
    }

    public void handleSingleVolume(BookMetaInfo metaInfo) throws InterruptedException {
        long start = System.currentTimeMillis();
        String bookId = metaInfo.getBid();
        String volumeNumber = metaInfo.getNumber();
        List<Catalog> catalogs = ApiUtils.getBookCatalog(bookId, volumeNumber);
        Config config = AppContext.getConfig();
        log.info("cate:{}", catalogs);
        int pages = metaInfo.getPages();
        Map<Integer, Integer> pageMap = new HashMap<>();
        for (int i = 1; i <= pages; i++) {
            pageMap.put(i, i);
        }
        File imageTempDir = null;
        if (StrUtil.isBlank(volumeNumber)) {
            imageTempDir = FileUtil.file(config.getWorkPath(), bookId);
        } else {
            imageTempDir = FileUtil.file(config.getWorkPath(), bookId, volumeNumber);
        }
        if (!imageTempDir.exists())
            imageTempDir.mkdirs();
        // 预先清理无效的图片
        Arrays.stream(Objects.requireNonNull(imageTempDir.listFiles())).forEach(file -> {
            long size = file.length();
            if (size <= Constants.IMG_INVALID_SIZE
                    || size == Constants.IMG_LOADING_SIZE) {
                FileUtil.del(file);
            }
        });
        //列出已经下载好的图片列表
        List<Integer> strings = FileUtil.listFileNames(imageTempDir.getAbsolutePath()).stream().map(
                fileName -> Integer.parseInt(fileName.substring(0, fileName.lastIndexOf('.')))
        ).sorted().collect(Collectors.toList());
        //之前成功下载的图片将会跳过
        List<Integer> failedImageList = pageMap.entrySet().stream().filter(entry -> !strings.contains(entry.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
        if (failedImageList.size() <= 0) {
            log.info("All image downloaded(total:{})", failedImageList.size());
            log.info("Ready for generate PDF");
            try {
                PDFUtils.gen(metaInfo, catalogs, imageTempDir.getAbsolutePath());
                log.info("All finished take :{}s", (System.currentTimeMillis() - start) / 1000);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            ThreadPoolExecutor executor = ThreadPoolUtils.getExecutor();
            log.info("Start download image, bookName:{},totalPage:{}", metaInfo.getName(), failedImageList.size());
            CountDownLatch latch = new CountDownLatch(failedImageList.size());
            for (Integer page : failedImageList) {
                FetchBookImageTask task = new FetchBookImageTask(imageTempDir.getAbsolutePath(), bookId, page, latch);
                task.setVolumeNumber(volumeNumber);
                executor.execute(task);
            }
            latch.await();
            long successCount = AppContext.getImageStatusMapping().entrySet().stream().filter(Map.Entry::getValue).count();
            if (successCount == failedImageList.size()) {
                log.info("All image downloaded");
                log.info("Ready for generate PDF");
                try {
                    PDFUtils.gen(metaInfo, catalogs, imageTempDir.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                log.info("All finished take :{}s", (System.currentTimeMillis() - start) / 1000);
            } else {
                AppContext.getImageStatusMapping().entrySet().stream().filter(entry -> !entry.getValue()).map(Map.Entry::getKey).forEach(pageNumber -> {
                    log.info("图片:{}下载失败", pageNumber);
                });
                log.info("请重新运行本程序下载");
            }
        }
    }

    public void handleMultipleVolume(List<BookMetaInfo> volumeList) throws InterruptedException {
        for (BookMetaInfo volume : volumeList) {
            handleSingleVolume(volume);
        }
    }

}
