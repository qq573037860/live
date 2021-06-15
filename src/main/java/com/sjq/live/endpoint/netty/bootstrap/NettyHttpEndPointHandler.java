package com.sjq.live.endpoint.netty.bootstrap;


public interface NettyHttpEndPointHandler {

    Object invoke(Object[] args);

    void invokeAsync(Object[] args);
}
