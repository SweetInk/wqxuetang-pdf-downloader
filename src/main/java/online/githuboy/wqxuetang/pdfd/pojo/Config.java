package online.githuboy.wqxuetang.pdfd.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 程序配置
 *
 * @author suchu
 * @since 2020年2月6日
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Config {
    /**
     * 最大线程数
     */
    private int threadCount = 1;
    /**
     * 程序工作路径
     */
    private String workPath;
    /**
     * 登录wqxuetang后的Cookie值
     */
    private String cookie;
    /**
     * 连续请求最大阈值数，程序将停顿后再执行
     */
    private Integer maxRequestThreshold = 45;
    /**
     * 超过一定请求后，程序停顿多少秒继续执行
     */
    private Integer waitingSeconds = 60;
    /**
     * 默认休眠时间
     */
    private Long defaultSleepTime = 1000L;
}
