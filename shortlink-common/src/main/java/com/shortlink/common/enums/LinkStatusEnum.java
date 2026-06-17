package com.shortlink.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Short link status enumeration.
 *
 * @author ShortLink
 */
@Getter
@AllArgsConstructor
public enum LinkStatusEnum {

    /** Link is active and can be accessed */
    NORMAL(0, "Normal"),

    /** Link has passed its expiration time */
    EXPIRED(1, "Expired"),

    /** Link has been manually disabled by the owner */
    DISABLED(2, "Disabled");

    private final int code;
    private final String desc;

    /**
     * Get enum by code value.
     *
     * @param code status code
     * @return matching enum, or NORMAL if not found
     */
    public static LinkStatusEnum fromCode(int code) {
        for (LinkStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return NORMAL;
    }
}
