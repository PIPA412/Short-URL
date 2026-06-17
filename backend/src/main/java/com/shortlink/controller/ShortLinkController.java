package com.shortlink.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.common.Result;
import com.shortlink.dto.CreateShortLinkDTO;
import com.shortlink.dto.ShortLinkVO;
import com.shortlink.dto.UpdateShortLinkDTO;
import com.shortlink.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/short-link")
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    @PostMapping
    public Result<ShortLinkVO> create(@Valid @RequestBody CreateShortLinkDTO dto,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(shortLinkService.create(dto, userId));
    }

    @GetMapping
    public Result<Page<ShortLinkVO>> page(@RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(shortLinkService.page(pageNum, pageSize, userId));
    }

    @GetMapping("/{id}")
    public Result<ShortLinkVO> getById(@PathVariable Long id) {
        return Result.success(shortLinkService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id,
                            @Valid @RequestBody UpdateShortLinkDTO dto,
                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        shortLinkService.update(id, dto, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        shortLinkService.delete(id, userId);
        return Result.success();
    }
}
