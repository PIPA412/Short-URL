package com.zsc.web.controller.common;

import com.zsc.common.annotation.Anonymous;
import com.zsc.module.common.response.ResultVo;
import com.zsc.module.domain.dto.SlShortLinkDto;
import com.zsc.module.service.SlShortLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 短链接公开生成控制器（无需登录）
 */
@Anonymous
@Tag(name = "短链接公开生成")
@Validated
@RestController
public class ShortLinkPublicController {

    @Autowired
    private SlShortLinkService slShortLinkService;

    /**
     * 公开生成短链接（无需登录）
     */
    @Operation(summary = "公开生成短链接")
    @PostMapping("/api/public/shortlink")
    public ResultVo<Map<String, String>> create(@Valid @RequestBody SlShortLinkDto dto) {
        String shortCode = slShortLinkService.addLink(dto);

        Map<String, String> result = new HashMap<>();
        result.put("shortCode", shortCode);
        result.put("shortUrl", "/s/" + shortCode);
        result.put("originalUrl", dto.getOriginalUrl());

        return ResultVo.ok(result);
    }
}
