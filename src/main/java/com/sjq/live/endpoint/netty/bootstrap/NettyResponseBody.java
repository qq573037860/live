package com.sjq.live.endpoint.netty.bootstrap;

import java.lang.annotation.*;

@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NettyResponseBody {
}
