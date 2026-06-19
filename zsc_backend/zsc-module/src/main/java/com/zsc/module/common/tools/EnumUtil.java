package com.zsc.module.common.tools;

public class EnumUtil {
    public static <E extends Enum<E> & BaseEnum> E getByCode(Class<E> enumClass, Integer code) {
        for (E e : enumClass.getEnumConstants()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}

