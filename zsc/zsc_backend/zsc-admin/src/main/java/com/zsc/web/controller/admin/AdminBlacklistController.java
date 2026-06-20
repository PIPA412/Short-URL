package com.zsc.web.controller.admin;

import com.zsc.common.annotation.Anonymous;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.module.domain.entity.SlBlacklistDomain;
import com.zsc.module.service.BlacklistDomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员 - 黑名单域名管理
 */
@Tag(name = "黑名单域名管理")
@Anonymous
@RestController
@RequestMapping("/admin/blacklist")
public class AdminBlacklistController {

    @Autowired
    private BlacklistDomainService blacklistDomainService;

    /**
     * 查看黑名单域名列表
     */
    @Operation(summary = "查看黑名单域名列表")
    @GetMapping("/list")
    public AjaxResult list() {
        List<SlBlacklistDomain> list = blacklistDomainService.listDomains();
        return AjaxResult.success(list);
    }

    /**
     * 添加黑名单域名
     */
    @Operation(summary = "添加黑名单域名")
    @PostMapping("/add")
    public AjaxResult add(@RequestBody SlBlacklistDomain domain) {
        blacklistDomainService.addDomain(domain.getDomain());
        return AjaxResult.success("添加成功");
    }

    /**
     * 删除黑名单域名
     */
    @Operation(summary = "删除黑名单域名")
    @DeleteMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        blacklistDomainService.removeDomain(id);
        return AjaxResult.success("删除成功");
    }
}