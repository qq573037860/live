package com.sjq.live.constant;

public enum SubscribeEnum {
    SUCCESS("存放订阅id", 0),
    NO_PUBLISHER("没有发布者", 1),
    SUBSCRIBED("已经订阅成过了", 2),
    ;

    private String name;
    private int code;

    SubscribeEnum(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public SubscribeEnum name(String name) {
        this.name = name;
        return this;
    }
}
