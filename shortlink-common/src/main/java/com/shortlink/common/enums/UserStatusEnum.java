package com.shortlink.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * User account status enumeration.
 *
 * @author ShortLink
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum {

    /** Account is active */
    OK(0, "OK"),

    /** Account has been disabled */
    DISABLED(1, "Disabled");

    private final int code;
    private final String desc;

    /**
     * Get enum by code value.
     *
     * @param code status code
     * @return matching enum, or OK if not found
     */
    public static UserStatusEnum fromCode(int code) {
        for (UserStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return OK;
    }
}
