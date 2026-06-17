package com.zsc.module.common.pagination;


/**
 * description:
 *
 * @author: hevean
 * @date: 2022/05/06
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 分页返回结果
 */
@Data
public class PageResult<T> {
    /** 当前页码*/
    private Long currentPage;
    /*** 每页数量*/
    private Long pageSize;
    /*** 记录总数*/
    private Long total;
    /*** 分页数据*/
    private List<T> list;


    /**将mybatis-plus的分页结果Page转换为PageResult类型,减少传给前端不必要的数据*/
    public static PageResult fromPage(Page page) {
        PageResult result = new PageResult();
        result.total = page.getTotal();
        result.pageSize = page.getSize();
        result.currentPage =page.getCurrent();
        result.list = page.getRecords();
        return result;
    }
}

