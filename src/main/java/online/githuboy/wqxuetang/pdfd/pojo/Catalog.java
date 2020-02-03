package online.githuboy.wqxuetang.pdfd.pojo;

import lombok.Data;

import java.util.List;

/**
 * 书籍目录信息
 * JSON format:
 * <pre>
 *
 * {children: [{id: "281543696187392", pid: "281474976710656", label: "1.1　计算机网络的产生与发展", pnum: "10", level: "2"},…]
 * id: "281474976710656"
 * label: "第1章　计算机网络概述"
 * level: "1"
 * pid: "0"
 * pnum: "10"
 * }</pre>
 *
 * @author suchu
 * @since 2020年2月3日
 */
@Data
public class Catalog {
    /**
     * 目录标题
     */
    private String label;
    /**
     * 对应页码
     */
    private Integer pnum;

    /**
     * 子目录项
     */
    private List<Catalog> children;
}
