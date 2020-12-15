package com.sjq.live.support;

import org.springframework.util.Assert;

import javax.servlet.ServletInputStream;
import java.io.IOException;

public class ServletInputStreamProcessor implements InputStreamProcessor {

    private final ServletInputStream inputStream;

    public ServletInputStreamProcessor(final ServletInputStream inputStream) {
        Assert.notNull(inputStream, "ServletInputStream不能为空");

        this.inputStream = inputStream;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        return inputStream.read(bytes);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
