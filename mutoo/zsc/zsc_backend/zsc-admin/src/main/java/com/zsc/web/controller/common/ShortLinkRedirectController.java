package com.zsc.web.controller.common;

import com.zsc.common.annotation.Anonymous;
import com.zsc.module.service.SlShortLinkService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * 短链接重定向控制器（公开访问）
 */
@Anonymous
@Controller
@RequestMapping("/s")
public class ShortLinkRedirectController {

    @Autowired
    private SlShortLinkService slShortLinkService;

    /**
     * 短链接重定向
     */
    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        String originalUrl = slShortLinkService.redirect(shortCode);

        if (originalUrl == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "短链接不存在或已失效");
            return;
        }

        // 302 临时重定向
        response.sendRedirect(originalUrl);
    }
}
