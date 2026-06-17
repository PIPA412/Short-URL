package com.zsc.common.utils;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsc.common.core.page.PageDomain;
import com.zsc.common.core.page.TableSupport;
import com.zsc.common.utils.sql.SqlUtil;

/**
 * 分页工具类
 * 
 * @author zsc
 */
public class PageUtils
{
    /**
     * 设置请求分页数据
     */
    public static <T> Page<T> startPage()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        
        Page<T> page = new Page<>(pageNum, pageSize);
        page.setSearchCount(true);
        
        if (reasonable != null)
        {
            page.setOptimizeCountSql(reasonable);
        }
        
        if (com.zsc.common.utils.StringUtils.isNotEmpty(orderBy))
        {
            String[] orderItems = orderBy.split(",");
            for (String orderItem : orderItems)
            {
                String[] parts = orderItem.trim().split("\\s+");
                if (parts.length >= 1)
                {
                    String column = parts[0];
                    boolean isAsc = parts.length == 1 || "asc".equalsIgnoreCase(parts[1]);
                    if (isAsc)
                    {
                        page.addOrder(OrderItem.asc(column));
                    }
                    else
                    {
                        page.addOrder(OrderItem.desc(column));
                    }
                }
            }
        }
        
        return page;
    }

    /**
     * 清理分页的线程变量
     */
    public static void clearPage()
    {
    }
}
