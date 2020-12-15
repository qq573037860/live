package com.sjq.live.model;

import com.sjq.live.constant.SubscribeEnum;

import java.util.Objects;

public class SubscribeResponse<T> {

    private boolean success;

    private int code;

    private String msg;

    private T data;

    public SubscribeResponse(SubscribeEnum subscribeEnum) {
        this.success = Objects.equals(SubscribeEnum.SUCCESS, subscribeEnum);
        this.code = subscribeEnum.getCode();
        this.msg = subscribeEnum.getName();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public SubscribeResponse<T> data(T data) {
        this.data = data;
        return this;
    }
}
