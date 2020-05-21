package com.sjq.live.support;

import java.io.IOException;

public interface InputStreamProcessor {

    int read(byte[] bytes) throws IOException;

    void close() throws IOException;

}
