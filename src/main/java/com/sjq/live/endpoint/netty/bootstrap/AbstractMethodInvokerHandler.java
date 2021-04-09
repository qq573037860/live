package com.sjq.live.endpoint.netty.bootstrap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractMethodInvokerHandler {

    protected final static ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(1,
            Runtime.getRuntime().availableProcessors(),
            6000L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    protected Object instance;
}
