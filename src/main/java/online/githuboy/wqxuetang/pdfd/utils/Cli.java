package online.githuboy.wqxuetang.pdfd.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.setting.dialect.Props;
import lombok.extern.slf4j.Slf4j;
import online.githuboy.wqxuetang.pdfd.App;
import online.githuboy.wqxuetang.pdfd.AppContext;
import online.githuboy.wqxuetang.pdfd.Constants;
import online.githuboy.wqxuetang.pdfd.CookieStore;
import online.githuboy.wqxuetang.pdfd.pojo.Config;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.Arrays;

/**
 * @author suchu
 * @since 2020年2月10日
 */
@Slf4j
public class Cli {
    private Options options = new Options();

    public Cli() {
        options.addOption(Option.builder("b").hasArg(true).required().desc("The id of book").build());
        options.addOption(Option.builder("c").hasArg(true).desc("Config file path.").build());
    }

    public CommandLine get(String[] args) {

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            log.info("Parse commandline args:{}", Arrays.toString(args));
            cmd = parser.parse(options, args);
            String bookId = cmd.getOptionValue("b");
            File configFile;
            String configPath = cmd.getOptionValue("c");
            if (null == configPath) {
                String jarPath = App.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                File classFile = new File(jarPath);
                if (classFile.isDirectory()) {
                    configFile = new File(classFile, Constants.DEFAULT_CONFIG_FILE);
                } else {
                    configFile = new File(classFile.getParent(), Constants.DEFAULT_CONFIG_FILE);
                }
            } else {
                configFile = FileUtil.file(configPath);
            }
            Config config = null;
            if (!configFile.isDirectory() && configFile.exists()) {
                Props props = new Props(configFile);
                config = props.toBean(Config.class, "config");
                config.setBookId(bookId);
                AppContext.setConfig(config);
                log.info("配置文件:{} 加载成功:\n{}", configFile.getAbsolutePath(), config);
            } else {
                log.error("配置文件:{}不存在，请检查路径是否正确", configFile.getAbsolutePath());
                return null;
            }
            CookieStore.COOKIE = config.getCookie();
            return cmd;
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            String footer = "\nPlease report issues at https://github.com/SweetInk/wqxuetang-pdf-downloader";
            formatter.printHelp("java -jar pdfd.jar", "\n", options, footer, true);
            return null;
        }
    }
}
