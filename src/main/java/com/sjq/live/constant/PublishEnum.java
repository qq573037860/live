package com.sjq.live.constant;

public enum PublishEnum {
    INVALID("无效订阅代码", -1),
    SUCCESS("发布成功", 0),
    DUPLICATE_PUBLISH("不能重复发布", 1)
    ;

    private String name;
    private int code;

    PublishEnum(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    public PublishEnum code(int code) {
        this.code = code;
        return this;
    }

    public static PublishEnum getByCode(final int code) {
        for (PublishEnum value : values()) {
            if (code == value.getCode()) {
                return value;
            }
        }
        return PublishEnum.INVALID.code(code);
    }
}
