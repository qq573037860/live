package com.sjq.live.constant;

public enum SubscribeEnum {
    INVALID("无效订阅代码", -1),
    SUCCESS("订阅成功", 0),
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

    public SubscribeEnum code(int code) {
        this.code = code;
        return this;
    }

    public static SubscribeEnum getByCode(final int code) {
        for (SubscribeEnum value : values()) {
            if (code == value.getCode()) {
                return value;
            }
        }
        return SubscribeEnum.INVALID.code(code);
    }
}
