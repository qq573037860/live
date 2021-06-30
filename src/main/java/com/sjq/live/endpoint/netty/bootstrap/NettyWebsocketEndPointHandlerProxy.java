package com.sjq.live.endpoint.netty.bootstrap;

import com.sjq.live.model.LiveException;
import com.sjq.live.model.NettyWebsocketContext;
import com.sjq.live.support.spring.SpringBeanUtil;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.*;

public class NettyWebsocketEndPointHandlerProxy implements NettyWebsocketEndPointHandler {

    private static final ExecutorService poolExecutor = new ThreadPoolExecutor(
            1,
            Runtime.getRuntime().availableProcessors(),
            6000L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1000),
            new DefaultThreadFactory("websocket任务线程", true),
            new ThreadPoolExecutor.CallerRunsPolicy());

    private final SpringBeanUtil.BeanFactory<NettyWebsocketEndPointHandler> beanFactory;

    public NettyWebsocketEndPointHandlerProxy(SpringBeanUtil.BeanFactory<NettyWebsocketEndPointHandler> beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public boolean beforeHandshake(final NettyWebsocketContext context) {
        return beanFactory.getBean().beforeHandshake(context);
    }

    @Override
    public void afterConnectionEstablished(final NettyWebsocketContext context) {
        beanFactory.getBean().afterConnectionEstablished(context);
    }

    @Override
    public void handleMessage(final NettyWebsocketContext context, byte[] data, boolean isLast) {
        poolExecutor.execute(() -> {
            try {
                beanFactory.getBean().handleMessage(context, data, isLast);
            } catch (Exception e) {
                throw new LiveException(e);
            }
        });
    }

    @Override
    public void handleMessage(final NettyWebsocketContext context, String data, boolean isLast) {
        poolExecutor.execute(() -> {
            try {
                beanFactory.getBean().handleMessage(context, data, isLast);
            } catch (Exception e) {
                throw new LiveException(e);
            }
        });
    }

    @Override
    public void afterConnectionClosed(final NettyWebsocketContext context) {
        beanFactory.getBean().afterConnectionClosed(context);
    }

}
