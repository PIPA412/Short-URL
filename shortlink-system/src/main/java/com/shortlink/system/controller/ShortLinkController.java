package com.shortlink.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortlink.common.annotation.Log;
import com.shortlink.common.core.PageResult;
import com.shortlink.common.core.R;
import com.shortlink.common.enums.BusinessTypeEnum;
import com.shortlink.system.security.ShortLinkUserDetails;
import com.shortlink.system.dto.req.ShortLinkCreateRequest;
import com.shortlink.system.dto.req.ShortLinkPageQuery;
import com.shortlink.system.dto.req.ShortLinkUpdateRequest;
import com.shortlink.system.dto.resp.ShortLinkPageItem;
import com.shortlink.system.dto.resp.ShortLinkResponse;
import com.shortlink.system.service.ShortLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Short link management controller.
 * <p>
 * Provides CRUD operations for short links. All endpoints require
 * authentication (configured in {@link com.shortlink.framework.security.SecurityConfig}).
 *
 * @author ShortLink
 */
@Tag(name = "Short Link", description = "Short link CRUD APIs")
@RestController
@RequestMapping("/api/short-link")
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    @Operation(summary = "Paginated list", description = "Query short links with pagination and filters")
    @GetMapping("/list")
    public R<PageResult<ShortLinkPageItem>> list(
            @AuthenticationPrincipal ShortLinkUserDetails userDetails,
            @Valid ShortLinkPageQuery query) {
        IPage<ShortLinkPageItem> page = shortLinkService.pageQuery(userDetails.getUserId(), query);
        return R.success(PageResult.build(page.getTotal(), page.getRecords()));
    }

    @Operation(summary = "Get link detail", description = "Get full short link information by ID")
    @GetMapping("/{id}")
    public R<ShortLinkResponse> getById(@PathVariable Long id) {
        return R.success(shortLinkService.getById(id));
    }

    @Operation(summary = "Create short link", description = "Create a new short link from a long URL")
    @Log(title = "Create Short Link", businessType = BusinessTypeEnum.INSERT)
    @PostMapping
    public R<ShortLinkResponse> create(
            @AuthenticationPrincipal ShortLinkUserDetails userDetails,
            @Valid @RequestBody ShortLinkCreateRequest request) {
        return R.success(shortLinkService.create(userDetails.getUserId(), request));
    }

    @Operation(summary = "Update short link", description = "Update an existing short link's properties")
    @Log(title = "Update Short Link", businessType = BusinessTypeEnum.UPDATE)
    @PutMapping
    public R<Void> update(
            @AuthenticationPrincipal ShortLinkUserDetails userDetails,
            @Valid @RequestBody ShortLinkUpdateRequest request) {
        shortLinkService.update(userDetails.getUserId(), request);
        return R.success();
    }

    @Operation(summary = "Delete short link", description = "Soft-delete a short link by ID")
    @Log(title = "Delete Short Link", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> delete(
            @AuthenticationPrincipal ShortLinkUserDetails userDetails,
            @PathVariable Long id) {
        shortLinkService.delete(userDetails.getUserId(), id);
        return R.success();
    }
}
