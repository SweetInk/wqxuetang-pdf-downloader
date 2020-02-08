package online.githuboy.wqxuetang.pdfd.pojo;

import lombok.Data;

import java.util.List;

/**
 * 书籍元数据实体
 * <p>
 * JSON format:
 * <pre>{bid: "2175744"
 * canread: 1
 * canreadpages: "311"
 * coverurl: "https://bookask-cover.oss-cn-beijing.aliyuncs.com/c/2/175/2175744/2175744.jpg!b"
 * ismultivolumed: "0"
 * last_volume: "1"
 * lastpage: "6"
 * name: "计算机网络基础"
 * pages: "311"
 * paperlowprice: "26.90"
 * paperurl: "http://product.dangdang.com/24011520.html?_ddclickunion=P-327429|ad_type=10|sys_id=1#dd_refer="
 * price: "39.00"
 * sellprice: "25.35"
 * textbook: "0"
 * title: "《计算机网络基础》 刘勇 邹广慧 【正版电子纸书阅读_PDF下载】- 书问"
 * toshelf: null
 * uid: null
 * upperlimit: 1
 * volume_list: []
 * }</pre>
 *
 * @author suchu
 * @since 2020年2月3日
 */
@Data
public class BookMetaInfo {
    /**
     * 书籍编号
     */
    private String bid;

    /**
     * 名称
     */
    private String name;

    /**
     * 页码数
     */
    private Integer pages;
    /**
     * 分册编号
     */
    private String number;

    /**
     * 书籍含多册信息
     */
    private List<BookMetaInfo> volumeList;

}
