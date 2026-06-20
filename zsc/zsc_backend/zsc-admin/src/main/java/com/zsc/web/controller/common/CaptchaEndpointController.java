package com.zsc.web.controller.common;

import com.zsc.common.annotation.Anonymous;
import com.zsc.common.core.domain.AjaxResult;
import com.zsc.common.utils.sign.Base64;
import com.zsc.framework.web.service.SimpleCaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

/**
 * 图形验证码 API（用于防机器人场景）
 * <p>
 * 提供验证码生成和校验接口，专用于以下场景：
 * <ul>
 *   <li>用户注册</li>
 *   <li>登录密码错误超过 3 次后</li>
 *   <li>短链接生成频率异常</li>
 * </ul>
 * </p>
 */
@Anonymous
@Tag(name = "图形验证码")
@RestController
@RequestMapping("/captcha")
public class CaptchaEndpointController {

    @Autowired
    private SimpleCaptchaService simpleCaptchaService;

    /**
     * 获取验证码图片
     * <p>
     * 返回 Base64 编码的图片和唯一标识 uuid。
     * 前端在提交时需将 uuid 和用户输入的 code 一起发送。
     * </p>
     */
    @Operation(summary = "获取验证码图片")
    @GetMapping("/image")
    public AjaxResult getCaptchaImage() throws IOException {
        Map<String, Object> captcha = simpleCaptchaService.generateCaptcha();

        String uuid = (String) captcha.get("uuid");
        BufferedImage image = (BufferedImage) captcha.get("image");

        // 将图片转为 Base64
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
        } catch (IOException e) {
            return AjaxResult.error("验证码生成失败");
        }

        AjaxResult result = AjaxResult.success();
        result.put("uuid", uuid);
        result.put("img", Base64.encode(os.toByteArray()));
        return result;
    }

    /**
     * 校验验证码
     */
    @Operation(summary = "校验验证码")
    @PostMapping("/verify")
    public AjaxResult verifyCaptcha(@RequestBody Map<String, String> params) {
        String uuid = params.get("uuid");
        String code = params.get("code");

        boolean passed = simpleCaptchaService.verifyCaptcha(uuid, code);

        if (passed) {
            return AjaxResult.success("验证码正确");
        } else {
            return AjaxResult.error("验证码错误或已过期");
        }
    }
}
