package com.zsc.framework.web.service;

import com.zsc.common.constant.CacheConstants;
import com.zsc.common.core.redis.RedisCache;
import com.zsc.common.utils.StringUtils;
import com.zsc.common.utils.uuid.IdUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 简单图形验证码服务
 * <p>
 * 生成 4 位字母+数字混合验证码，存入 Redis（5 分钟有效期）。
 * 提供生成和校验两个核心方法。
 * </p>
 */
@Service
public class SimpleCaptchaService {

    /** 验证码有效期（分钟） */
    private static final long CAPTCHA_TTL_MINUTES = 5;

    @Resource(name = "captchaProducerSimple")
    private com.google.code.kaptcha.Producer captchaProducer;

    @Autowired
    private RedisCache redisCache;

    /**
     * 生成验证码
     *
     * @return Map 包含 uuid（用于后续校验）和 image（BufferedImage）
     */
    public Map<String, Object> generateCaptcha() {
        // 生成验证码文本（4 位字母数字）
        String code = captchaProducer.createText();

        // 生成唯一标识
        String uuid = IdUtils.simpleUUID();

        // 存入 Redis
        String redisKey = CacheConstants.SIMPLE_CAPTCHA_KEY + uuid;
        redisCache.setCacheObject(redisKey, code, (int) CAPTCHA_TTL_MINUTES, TimeUnit.MINUTES);

        // 生成图片
        BufferedImage image = captchaProducer.createImage(code);

        Map<String, Object> result = new HashMap<>();
        result.put("uuid", uuid);
        result.put("image", image);
        return result;
    }

    /**
     * 校验验证码
     *
     * @param uuid 验证码唯一标识
     * @param code 用户输入的验证码
     * @return true 验证通过，false 验证失败（含过期）
     */
    public boolean verifyCaptcha(String uuid, String code) {
        if (StringUtils.isBlank(uuid) || StringUtils.isBlank(code)) {
            return false;
        }

        String redisKey = CacheConstants.SIMPLE_CAPTCHA_KEY + uuid;
        String correctCode = redisCache.getCacheObject(redisKey);

        if (correctCode == null) {
            // 验证码不存在或已过期
            return false;
        }

        // 验证后立即删除（一次性使用）
        redisCache.deleteObject(redisKey);

        // 忽略大小写
        return code.equalsIgnoreCase(correctCode);
    }

    /**
     * 校验验证码（不删除，用于验证完成后还需继续操作的场景）
     *
     * @param uuid 验证码唯一标识
     * @param code 用户输入的验证码
     * @return true 验证通过
     */
    public boolean verifyCaptchaKeepCode(String uuid, String code) {
        if (StringUtils.isBlank(uuid) || StringUtils.isBlank(code)) {
            return false;
        }

        String redisKey = CacheConstants.SIMPLE_CAPTCHA_KEY + uuid;
        String correctCode = redisCache.getCacheObject(redisKey);

        if (correctCode == null) {
            return false;
        }

        return code.equalsIgnoreCase(correctCode);
    }
}
