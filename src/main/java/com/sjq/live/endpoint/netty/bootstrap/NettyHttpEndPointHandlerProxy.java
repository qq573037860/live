package com.sjq.live.endpoint.netty.bootstrap;

import com.sjq.live.model.LiveException;
import com.sjq.live.model.NettyHttpContext;
import com.sjq.live.model.NettyHttpRequest;
import com.sjq.live.support.netty.NettyOutputStream;
import com.sjq.live.support.spring.SpringBeanUtil;
import com.sjq.live.utils.proxy.Wrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NettyHttpEndPointHandlerProxy implements NettyHttpEndPointHandler {

    private static final ExecutorService poolExecutor = new ThreadPoolExecutor(
            1,
            Runtime.getRuntime().availableProcessors(),
            6000L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1000),
            new DefaultThreadFactory("http任务线程", true), new ThreadPoolExecutor.CallerRunsPolicy());

    private SpringBeanUtil.BeanWrapper instanceWrapper;

    private Wrapper wrapper;

    private Method method;

    private boolean isStaticResourceHandler;

    public NettyHttpEndPointHandlerProxy(Wrapper wrapper,
                                         SpringBeanUtil.BeanWrapper instanceWrapper,
                                         Method method) {
        this.wrapper = wrapper;
        this.instanceWrapper = instanceWrapper;
        this.method = method;
    }

    public NettyHttpEndPointHandlerProxy(boolean isStaticResourceHandler) {
        this.isStaticResourceHandler = isStaticResourceHandler;
    }

    @Override
    public void invoke(NettyHttpRequest nettyHttpRequest, ChannelHandlerContext ctx) {
        //处理静态资源请求
        if (isStaticResourceHandler) {
            DefaultNettyHttpResponseProcessor.processStaticResourceResponse(ctx, nettyHttpRequest, nettyHttpRequest.getPath());
            return;
        }

        Object result;
        try {
            result = wrapper.invokeMethod(instanceWrapper.getBean(), method.getName(), method.getParameterTypes(), new Object[]{new NettyHttpContext(new NettyOutputStream(ctx), nettyHttpRequest)});
        } catch (Exception e) {
            throw new LiveException(e);
        }

        DefaultNettyHttpResponseProcessor.processResponse(method, nettyHttpRequest, result, ctx);
    }

    @Override
    public void invokeAsync(NettyHttpRequest nettyHttpRequest, ChannelHandlerContext ctx) {
        poolExecutor.execute(() -> invoke(nettyHttpRequest, ctx));
    }

    protected Wrapper getEndpointWrapper() {
        return wrapper;
    }
}
