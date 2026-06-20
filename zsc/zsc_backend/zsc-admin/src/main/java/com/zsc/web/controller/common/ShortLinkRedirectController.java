package com.zsc.web.controller.common;

import com.zsc.common.annotation.Anonymous;
import com.zsc.common.utils.ip.IpUtils;
import com.zsc.module.common.exception.ServiceException;
import com.zsc.module.service.ShortLinkRedirectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

/**
 * 短链接重定向控制器（公开访问）
 *
 * <p>通过短码 302 重定向到原始 URL。支持访问密码验证（?pwd=xxx）。
 * 流程：查缓存/DB → 校验状态/过期/次数/密码 → 更新点击数 → 异步记录日志 → 302 跳转</p>
 */
@Anonymous
@Controller
@RequestMapping("/")
public class ShortLinkRedirectController {

    private static final Logger log = LoggerFactory.getLogger(ShortLinkRedirectController.class);

    @Autowired
    private ShortLinkRedirectService shortLinkRedirectService;

    /**
     * 短链接重定向入口
     *
     * @param shortCode 短码
     * @param password  访问密码（可选，通过 ?pwd=xxx 传入）
     * @param request   HTTP 请求（用于提取 IP、UA、Referer）
     * @param response  HTTP 响应（用于 302 跳转或返回错误）
     */
    @GetMapping("/{shortCode}")
    public void redirect(@PathVariable String shortCode,
                         @RequestParam(value = "pwd", required = false) String password,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        // 提取请求信息
        String ip = IpUtils.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");

        log.debug("短链接重定向请求: shortCode={}, ip={}, referer={}", shortCode, ip, referer);

        try {
            // 执行重定向逻辑（含缓存查询 + 有效性校验 + 更新点击量 + 异步日志）
            String originalUrl = shortLinkRedirectService.redirect(
                    shortCode, password, ip, userAgent, referer);

            if (originalUrl == null) {
                // 短链接不存在或已失效
                log.warn("短链接不存在或已失效: shortCode={}", shortCode);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "短链接不存在或已失效");
                return;
            }

            log.info("短链接重定向成功: {} → {}", shortCode, originalUrl);

            // 302 临时重定向
            response.sendRedirect(originalUrl);

        } catch (ServiceException e) {
            // 业务异常（如密码错误）
            if ("访问密码错误".equals(e.getMessage())) {
                log.warn("短链接密码错误: shortCode={}", shortCode);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "该短链接需要访问密码");
            } else {
                log.error("短链接重定向异常: shortCode={}, error={}", shortCode, e.getMessage());
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "系统错误");
            }
        }
    }
}
