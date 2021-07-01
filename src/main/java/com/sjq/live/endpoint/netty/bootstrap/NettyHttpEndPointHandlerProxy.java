package com.sjq.live.endpoint.netty.bootstrap;

import com.sjq.live.model.LiveException;
import com.sjq.live.model.NettyHttpContext;
import com.sjq.live.model.NettyHttpRequest;
import com.sjq.live.support.netty.NettyChannelAttribute;
import com.sjq.live.support.netty.NettyOutputStream;
import com.sjq.live.support.spring.SpringBeanUtil;
import com.sjq.live.utils.MethodUtil;
import com.sjq.live.utils.proxy.Wrapper;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NettyHttpEndPointHandlerProxy implements NettyHttpEndPointHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpEndPointHandlerProxy.class);

    /**
     * 短连接线程池
     */
    private static final ExecutorService SHORT_LINK_POOL_EXECUTOR = new ThreadPoolExecutor(
            1,
            Runtime.getRuntime().availableProcessors(),
            6000L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1000),
            new DefaultThreadFactory("http短连接工作线程", true),
            new ThreadPoolExecutor.AbortPolicy());

    /**
     * 长连接线程池
     */
    private static final ExecutorService LONG_LINK_POOL_EXECUTOR = new ThreadPoolExecutor(
            1,
            Runtime.getRuntime().availableProcessors(),
            6000L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1000),
            new DefaultThreadFactory("http长连接工作线程", true),
            new ThreadPoolExecutor.AbortPolicy());

    private SpringBeanUtil.BeanFactory beanFactory;

    private Wrapper invokerHandler;

    private Method method;

    private final boolean isStaticResourceHandler;

    private final boolean isLongLink;

    public NettyHttpEndPointHandlerProxy(Wrapper invokerHandler,
                                         SpringBeanUtil.BeanFactory beanFactory,
                                         Method method,
                                         boolean isLongLink) {
        this.invokerHandler = invokerHandler;
        this.beanFactory = beanFactory;
        this.method = method;
        this.isStaticResourceHandler = false;
        this.isLongLink = isLongLink;
    }

    public NettyHttpEndPointHandlerProxy(boolean isStaticResourceHandler) {
        this.isStaticResourceHandler = isStaticResourceHandler;
        this.method = MethodUtil.getDemoMethod();
        this.isLongLink = false;
    }

    @Override
    public void invoke(NettyHttpRequest nettyHttpRequest, ChannelHandlerContext ctx) {
        Object result;
        if (isStaticResourceHandler) {
            // 静态资源请求直接拼接需要返回的资源路径
            result = "static" + nettyHttpRequest.getPath();
        } else {
            try {
                result = invokerHandler.invokeMethod(
                            beanFactory.getBean(),
                            method.getName(),
                            method.getParameterTypes(),
                            new Object[]{new NettyHttpContext(new NettyOutputStream(ctx), nettyHttpRequest)});
            } catch (Exception e) {
                throw new LiveException(e);
            }
        }

        //处理response
        NettyHttpResponseProcessor.processResponse(method, nettyHttpRequest, result, ctx);

        //关闭channel
        if (!nettyHttpRequest.isKeepAlive()) {
            ChannelFuture future = NettyChannelAttribute.getLastChannelFuture(ctx);
            if (Objects.nonNull(future)) {
                future.addListener(ChannelFutureListener.CLOSE);
            } else {
                logger.warn("ctx[{}] LastChannelFuture is empty", ctx);
            }
        }
    }

    @Override
    public void invokeAsync(NettyHttpRequest nettyHttpRequest, ChannelHandlerContext ctx) {
        if (isLongLink) {
            LONG_LINK_POOL_EXECUTOR.execute(() -> invoke(nettyHttpRequest, ctx));
        } else {
            SHORT_LINK_POOL_EXECUTOR.execute(() -> invoke(nettyHttpRequest, ctx));
        }
    }
}
