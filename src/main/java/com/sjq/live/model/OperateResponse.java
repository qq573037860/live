package com.sjq.live.model;

import com.sjq.live.constant.PublishEnum;
import com.sjq.live.constant.SubscribeEnum;

import java.util.Objects;

public class OperateResponse<T> {

    private boolean success;

    private int code;

    private String msg;

    private T data;

    public OperateResponse(SubscribeEnum subscribeEnum) {
        this.success = Objects.equals(SubscribeEnum.SUCCESS, subscribeEnum);
        this.code = subscribeEnum.getCode();
        this.msg = subscribeEnum.getName();
    }

    public OperateResponse(SubscribeEnum subscribeEnum, T data) {
        this.success = Objects.equals(SubscribeEnum.SUCCESS, subscribeEnum);
        this.code = subscribeEnum.getCode();
        this.msg = subscribeEnum.getName();
        this.data = data;
    }

    public OperateResponse(PublishEnum publishEnum) {
        this.success = Objects.equals(PublishEnum.SUCCESS, publishEnum);
        this.code = publishEnum.getCode();
        this.msg = publishEnum.getName();
    }

    public OperateResponse(PublishEnum publishEnum, T data) {
        this.success = Objects.equals(PublishEnum.SUCCESS, publishEnum);
        this.code = publishEnum.getCode();
        this.msg = publishEnum.getName();
        this.data = data;
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

    public void setData(T data) {
        this.data = data;
    }
}
