package com.zsc.module.controller;

import com.zsc.common.annotation.Log;
import com.zsc.common.enums.BusinessType;
import com.zsc.module.common.pagination.PageResult;
import com.zsc.module.common.response.ResultVo;
import com.zsc.module.domain.dto.SlShortLinkDto;
import com.zsc.module.domain.dto.query.SlShortLinkQueryDto;
import com.zsc.module.domain.entity.SlShortLink;
import com.zsc.module.service.SlShortLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 短链接管理控制器
 */
@Tag(name = "短链接管理")
@Validated
@RestController
@RequestMapping("/api/shortlink")
public class SlShortLinkController {

    @Autowired
    private SlShortLinkService slShortLinkService;

    /**
     * 获取指定ID的短链接信息
     */
    @Operation(summary = "获取短链接详情")
    @PreAuthorize("@ss.hasPermi('biz:shortlink:query')")
    @GetMapping("/{id}")
    public ResultVo<SlShortLink> get(@PathVariable long id) {
        SlShortLink link = slShortLinkService.getById(id);
        return ResultVo.ok(link);
    }

    /**
     * 添加新的短链接
     */
    @Operation(summary = "新增短链接")
    @PreAuthorize("@ss.hasPermi('biz:shortlink:add')")
    @Log(title = "短链接管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ResultVo add(@Valid @RequestBody SlShortLinkDto dto) {
        slShortLinkService.addLink(dto);
        return ResultVo.ok();
    }

    /**
     * 更新短链接信息
     */
    @Operation(summary = "修改短链接")
    @PreAuthorize("@ss.hasPermi('biz:shortlink:edit')")
    @Log(title = "短链接管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public ResultVo update(@Valid @RequestBody SlShortLinkDto dto) {
        slShortLinkService.updateLink(dto);
        return ResultVo.ok();
    }

    /**
     * 删除指定ID的短链接
     */
    @Operation(summary = "删除短链接")
    @PreAuthorize("@ss.hasPermi('biz:shortlink:remove')")
    @Log(title = "短链接管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public ResultVo delete(@PathVariable @Min(value = 1, message = "链接ID不能小于1") long id) {
        return slShortLinkService.removeById(id)
                ? ResultVo.ok("删除成功")
                : ResultVo.fail("数据不存在，删除失败");
    }

    /**
     * 获取短链接统计数据
     */
    @Operation(summary = "获取短链接统计")
    @PreAuthorize("@ss.hasPermi('biz:shortlink:query')")
    @GetMapping("/stats/{id}")
    public ResultVo<java.util.Map<String, Object>> stats(@PathVariable long id) {
        return ResultVo.ok(slShortLinkService.getLinkStats(id));
    }

    /**
     * 分页查询短链接列表
     */
    @Operation(summary = "查询短链接列表")
    @PreAuthorize("@ss.hasPermi('biz:shortlink:list')")
    @PostMapping("/query")
    public ResultVo<PageResult> query(@RequestBody SlShortLinkQueryDto queryDto) {
        return ResultVo.ok(slShortLinkService.queryLinks(queryDto));
    }
}
