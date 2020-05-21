package com.sjq.live.support;

import javax.servlet.ServletInputStream;
import java.io.IOException;

public class ServletInputStreamProcessor implements InputStreamProcessor {

    private ServletInputStream inputStream;

    public ServletInputStreamProcessor(ServletInputStream inputStream) {
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
