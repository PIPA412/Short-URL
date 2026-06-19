package com.zsc.module.common.tools;

import java.util.regex.Pattern;

public class CronUtils {
    private static final String CRON_PATTERN = "^(*|(d{1,2}(,d{1,2})*|(d{1,2}-d{1,2})|(d{1,2}/d{1,2})))(s+(*|(d{1,2}(,d{1,2})*|(d{1,2}-d{1,2})|(d{1,2}/d{1,2})))){5}(s+(*|(d{1,4}(,d{1,4})*|(d{1,4}-d{1,4})|(d{1,4}/d{1,4}))))?$";
    private static final Pattern pattern = Pattern.compile(CRON_PATTERN);

    public static boolean isValidCron(String cronExpression) {
        return pattern.matcher(cronExpression).matches();
    }
}

