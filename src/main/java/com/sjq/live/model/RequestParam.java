package com.sjq.live.model;

import com.sjq.live.support.PublishHandler;
import com.sjq.live.support.SubscribeHandler;
import org.springframework.util.Assert;

import java.util.Map;

public class RequestParam {

    private final Map<String, Object> attributeMap;

    public RequestParam(final Map<String, Object> attributeMap) {
        Assert.notNull(attributeMap, "attributeMap不能为空");

        this.attributeMap = attributeMap;
    }

    public String getPublishId() {
        return (String) attributeMap.get("publishId");
    }

    public String getSubscribeId() {
        return (String) attributeMap.get("subscribeId");
    }

    public String getRegisterId() {
        return (String) attributeMap.get("registerId");
    }

    public String getUserId() {
        return (String) attributeMap.get("userId");
    }

    public PublishHandler getPublishHandler () {
        return (PublishHandler) attributeMap.get("publishHandler");
    }

    public SubscribeHandler getSubscribeHandler () {
        return (SubscribeHandler) attributeMap.get("subscribeHandler");
    }

    public void setPublishId(final String publishId) {
        attributeMap.put("publishId", publishId);
    }

    public void setSubscribeId(final String subscribeId) {
        attributeMap.put("subscribeId", subscribeId);
    }

    public void setRegisterId(final String registerId) {
        attributeMap.put("registerId", registerId);
    }

    public void setUserId(final String userId) {
        attributeMap.put("userId", userId);
    }

    public void setPublishHandler (final PublishHandler publishHandler) {
        attributeMap.put("publishHandler", publishHandler);
    }

    public void setSubscribeHandler (final SubscribeHandler subscribeHandler) {
        attributeMap.put("subscribeHandler", subscribeHandler);
    }
}
