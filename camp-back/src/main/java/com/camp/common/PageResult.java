package com.camp.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回封装
 *
 * @param <T> 数据类型
 * @author camp
 */
@Data
public class PageResult<T> implements Serializable {

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Long current;

    /** 每页条数 */
    private Long size;

    /** 总页数 */
    private Long pages;

    /** 数据列表 */
    private List<T> records;

    /**
     * 由 MyBatis-Plus 的分页结果转换而来
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());
        result.setRecords(page.getRecords());
        return result;
    }
}
