package com.zsc.common.constant;

/**
 * 缓存的key 常量
 * 
 * @author zsc
 */
public class CacheConstants
{
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    /**
     * 速率限制 redis key（按用户）
     */
    public static final String RATE_LIMIT_USER_KEY = "rate:user:";

    /**
     * 速率限制 redis key（按 IP）
     */
    public static final String RATE_LIMIT_IP_KEY = "rate:ip:";

    /**
     * 短链接生成连续次数计数器（用于触发验证码）
     */
    public static final String SHORT_LINK_COUNTER_KEY = "shortlink:gen:ip:";

    /**
     * 图形验证码（简单验证码） redis key
     */
    public static final String SIMPLE_CAPTCHA_KEY = "simple_captcha:";
}
