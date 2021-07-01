package com.sjq.live.endpoint.netty.bootstrap;

import org.springframework.http.HttpMethod;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NettyEndPoint {

    String path() default "";

    /**
     * 该字段只在修饰方法的时候生效
     * @return
     */
    HttpMethod method() default HttpMethod.GET;

    boolean isLongLink() default false;
}
