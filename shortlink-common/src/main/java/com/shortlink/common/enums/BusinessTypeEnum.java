package com.shortlink.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Business operation type for AOP logging.
 *
 * @author ShortLink
 */
@Getter
@AllArgsConstructor
public enum BusinessTypeEnum {

    /** Insert / create operation */
    INSERT(0, "Insert"),

    /** Update / modify operation */
    UPDATE(1, "Update"),

    /** Delete / remove operation */
    DELETE(2, "Delete"),

    /** Other operation types (query, export, etc.) */
    OTHER(3, "Other");

    private final int code;
    private final String desc;
}
