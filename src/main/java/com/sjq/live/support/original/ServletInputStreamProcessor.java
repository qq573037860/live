package com.sjq.live.support.original;

import com.sjq.live.support.InputStreamProcessor;
import org.springframework.util.Assert;

import javax.servlet.ServletInputStream;
import java.io.IOException;

public class ServletInputStreamProcessor implements InputStreamProcessor {

    private final ServletInputStream inputStream;
    private final byte[] data = new byte[1024*1024];

    public ServletInputStreamProcessor(final ServletInputStream inputStream) {
        Assert.notNull(inputStream, "ServletInputStream不能为空");

        this.inputStream = inputStream;
    }

    @Override
    public byte[] read() throws IOException {
        int len = inputStream.read(data);
        if (-1 == len) {
            return null;
        }
        if (len == data.length) {
            return data;
        }
        byte[] newData = new byte[len];
        System.arraycopy(data, 0, newData, 0, len);
        return newData;
    }

    @Override
    public void close() throws IOException {
    }
}
