package com.sjq.live.support;

import org.springframework.util.Assert;

import java.util.Objects;

public class PublishHandler {

    private final OutputStreamProcessor out;
    private final CallBack closeCallBack;

    public PublishHandler(final OutputStreamProcessor out,
                          final CallBack callBack) {
        Assert.notNull(out, "OutputStreamProcessor不能为空");

        this.out = out;
        this.closeCallBack = callBack;
    }

    public void write(final byte[] bytes) {
        out.write(bytes);
    }

    public void close() {
        out.close();
        if (Objects.nonNull(closeCallBack)) {
            closeCallBack.call();
        }
    }
}
