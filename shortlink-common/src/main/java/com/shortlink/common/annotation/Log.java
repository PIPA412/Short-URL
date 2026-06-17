package com.shortlink.common.annotation;

import com.shortlink.common.enums.BusinessTypeEnum;

import java.lang.annotation.*;

/**
 * Custom annotation for operation logging (AOP pointcut).
 * <p>
 * Annotate a controller method to automatically log:
 * <ul>
 *   <li>Method invocation — class name, method name, parameters</li>
 *   <li>Execution result — return value or error</li>
 *   <li>Elapsed time</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>{@code
 *   @Log(title = "Create Short Link", businessType = BusinessTypeEnum.INSERT)
 *   @PostMapping
 *   public R<Void> create(...) { ... }
 * }</pre>
 *
 * @author ShortLink
 * @see com.shortlink.framework.aspect.LogAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /** Operation title / description */
    String title() default "";

    /** Business operation type */
    BusinessTypeEnum businessType() default BusinessTypeEnum.OTHER;

    /** Whether to save the request parameters in the log */
    boolean isSaveRequestData() default true;

    /** Whether to save the response data in the log */
    boolean isSaveResponseData() default false;
}
