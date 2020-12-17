package com.sjq.live.model;

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

    public R getPublishHandler () {
        return (R) attributeMap.get("publishHandler");
    }

    public R getSubscribeHandler () {
        return (R) attributeMap.get("subscribeHandler");
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

    public void setPublishHandler (final T publishHandler) {
        attributeMap.put("publishHandler", publishHandler);
    }

    public void setSubscribeHandler (final T subscribeHandler) {
        attributeMap.put("subscribeHandler", subscribeHandler);
    }
}
