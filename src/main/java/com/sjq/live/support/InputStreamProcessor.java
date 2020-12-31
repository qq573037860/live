package com.sjq.live.support;

import java.io.IOException;

public interface InputStreamProcessor {

    byte[] read() throws IOException;

    void close() throws IOException;

}
