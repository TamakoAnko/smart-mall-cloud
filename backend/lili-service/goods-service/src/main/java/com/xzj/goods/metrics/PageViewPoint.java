package com.xzj.goods.metrics;

import com.xzj.goods.metrics.enums.PageViewEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks buyer endpoints whose page view should be counted. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PageViewPoint {
    PageViewEnum type();
    String id() default "";
}
