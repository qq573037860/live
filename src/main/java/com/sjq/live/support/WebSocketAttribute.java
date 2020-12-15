package com.sjq.live.support;

import org.springframework.util.Assert;

import java.util.Map;

public class WebSocketAttribute<T , R> {

    private final Map<String, T> attributeMap;

    public WebSocketAttribute(final Map<String, T> attributeMap) {
        Assert.notNull(attributeMap, "attributeMap不能为空");

        this.attributeMap = attributeMap;
    }

    public R getPublishId() {
        return (R) attributeMap.get("publishId");
    }

    public R getSubscribeId() {
        return (R) attributeMap.get("subscribeId");
    }

    public R getRegisterId() {
        return (R) attributeMap.get("registerId");
    }

    public R getUserId() {
        return (R) attributeMap.get("userId");
    }

    public R getStreamWriteHandler () {
        return (R) attributeMap.get("streamHandler");
    }

    public void setPublishId(final T publishId) {
        attributeMap.put("publishId", publishId);
    }

    public void setSubscribeId(final T subscribeId) {
        attributeMap.put("subscribeId", subscribeId);
    }

    public void setRegisterId(final T registerId) {
        attributeMap.put("registerId", registerId);
    }

    public void setUserId(final T userId) {
        attributeMap.put("userId", userId);
    }

    public void setStreamWriteHandler (final T streamHandler) {
        attributeMap.put("streamHandler", streamHandler);
    }
}
