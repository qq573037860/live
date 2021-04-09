package com.sjq.live.support;

import org.springframework.util.Assert;

public class SubscribeHandler {

    private final AbstractStreamDistributeHandler streamDistributeHandler;

    public SubscribeHandler(final AbstractStreamDistributeHandler streamDistributeHandler) {
        Assert.notNull(streamDistributeHandler, "AbstractStreamDistributeHandler不能为空");

        this.streamDistributeHandler = streamDistributeHandler;
    }

    public void unSubscribe() {
        streamDistributeHandler.destroy();
    }

}
