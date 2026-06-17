package com.shortlink.framework.aspect;

import com.shortlink.common.annotation.Log;
import com.shortlink.common.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * AOP aspect for {@link Log} annotation.
 * <p>
 * Wraps annotated controller methods to log:
 * <ul>
 *   <li>Request details: IP, method, URI</li>
 *   <li>Method signature: class name, method name, parameters</li>
 *   <li>Execution result or error</li>
 *   <li>Elapsed time in milliseconds</li>
 * </ul>
 *
 * @author ShortLink
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    /**
     * Around advice for methods annotated with {@code @Log}.
     */
    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        long startTime = System.currentTimeMillis();

        // Extract method info
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();

        // Extract request info
        ServletRequestAttributes attrs = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        String requestUri = "";
        String ip = "";
        String httpMethod = "";
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            requestUri = request.getRequestURI();
            ip = IpUtils.getIpAddr(request);
            httpMethod = request.getMethod();
        }

        // Log request parameters if configured
        if (logAnnotation.isSaveRequestData()) {
            Object[] args = joinPoint.getArgs();
            log.info("[OP-LOG] [{}/{}] {} {} | IP: {} | Title: {} | Args: {}",
                    httpMethod, requestUri, className, methodName, ip,
                    logAnnotation.title(), args);
        } else {
            log.info("[OP-LOG] [{}/{}] {} {} | IP: {} | Title: {}",
                    httpMethod, requestUri, className, methodName, ip,
                    logAnnotation.title());
        }

        Object result;
        try {
            result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - startTime;

            // Log response if configured
            if (logAnnotation.isSaveResponseData()) {
                log.info("[OP-LOG] {} {} completed in {}ms | Result: {}",
                        className, methodName, elapsed, result);
            } else {
                log.info("[OP-LOG] {} {} completed in {}ms",
                        className, methodName, elapsed);
            }
        } catch (Throwable e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[OP-LOG] {} {} FAILED after {}ms | Error: {}",
                    className, methodName, elapsed, e.getMessage());
            throw e;
        }

        return result;
    }
}
