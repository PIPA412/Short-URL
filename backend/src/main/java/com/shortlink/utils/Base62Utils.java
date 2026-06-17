package com.shortlink.utils;

public final class Base62Utils {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final long BASE = 62;

    private Base62Utils() {}

    public static String encode(long value) {
        if (value < 0) {
            value = -value;
        }
        if (value == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.insert(0, BASE62.charAt((int) (value % BASE)));
            value /= BASE;
        }
        return sb.toString();
    }

    public static long decode(String base62) {
        long result = 0;
        for (int i = 0; i < base62.length(); i++) {
            int digit = BASE62.indexOf(base62.charAt(i));
            if (digit < 0) {
                throw new IllegalArgumentException("非法Base62字符: " + base62.charAt(i));
            }
            result = result * BASE + digit;
        }
        return result;
    }
}
