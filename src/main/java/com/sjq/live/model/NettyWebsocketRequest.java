package com.sjq.live.model;


import java.util.Map;

public class NettyWebsocketRequest {

    private Map<String, Object> attribute;

    private String path;

    public Map<String, Object> getAttribute() {
        return attribute;
    }

    public void setAttribute(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
