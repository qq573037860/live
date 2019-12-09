package com.sjq.live.constant;

public enum SubscribeEnum {
    SUCCESS("", 0),//name:对应订阅id
    NO_PUBLISHER("没有发布者", 1),
    SUBSCRIBED("已经订阅成过了", 2),
    READ_HANDLER_IS_NULL("readHandler为空", 3),
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
