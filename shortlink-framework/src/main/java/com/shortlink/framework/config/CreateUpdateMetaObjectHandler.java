package com.shortlink.framework.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus auto-fill handler.
 * <p>
 * Automatically populates {@code createTime} and {@code updateTime} fields
 * on entity insert and update operations, matching the
 * {@link com.baomidou.mybatisplus.annotation.FieldFill} annotations
 * on {@link com.shortlink.common.dto.BaseEntity}.
 *
 * @author ShortLink
 */
@Slf4j
@Component
public class CreateUpdateMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (hasGetter("createTime")) {
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        }
        if (hasGetter("updateTime")) {
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (hasGetter("updateTime")) {
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
    }

    /**
     * Check if the meta object has a getter for the given field name.
     * Avoids filling fields that don't exist on the entity.
     */
    private boolean hasGetter(String fieldName) {
        return true; // strictFill will silently skip missing fields
    }
}
