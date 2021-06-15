package com.sjq.live.endpoint.netty.bootstrap;

import com.sjq.live.model.LiveException;
import com.sjq.live.support.spring.SpringBeanUtil;
import com.sjq.live.utils.proxy.Wrapper;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NettyHttpEndPointHandlerProxy implements NettyHttpEndPointHandler {

    private final ExecutorService poolExecutor;

    private final SpringBeanUtil.BeanWrapper instanceWrapper;

    private final Wrapper wrapper;

    private final String methodName;

    private final Class<?>[] argsTypes;

    public NettyHttpEndPointHandlerProxy(Wrapper wrapper,
                                         SpringBeanUtil.BeanWrapper instanceWrapper,
                                         String methodName,
                                         Class<?>[] argsTypes) {
        this.wrapper = wrapper;
        this.instanceWrapper = instanceWrapper;
        this.methodName = methodName;
        this.argsTypes = argsTypes;
        this.poolExecutor = new ThreadPoolExecutor(1, Runtime.getRuntime().availableProcessors(),
                6000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1000),
                new DefaultThreadFactory("http任务线程", true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public Object invoke(Object[] args) {
        try {
            return wrapper.invokeMethod(instanceWrapper.getBean(), methodName, argsTypes, args);
        } catch (Exception e) {
            throw new LiveException(e);
        }
    }

    @Override
    public void invokeAsync(Object[] args) {
        poolExecutor.execute(() -> {
            try {
                wrapper.invokeMethod(instanceWrapper.getBean(), methodName, argsTypes, args);
            } catch (Exception e) {
                throw new LiveException(e);
            }
        });
    }

    protected Wrapper getEndpointWrapper() {
        return wrapper;
    }

    protected String getMethodName() {
        return methodName;
    }

    protected Class<?>[] getArgsTypes() {
        return argsTypes;
    }

}
