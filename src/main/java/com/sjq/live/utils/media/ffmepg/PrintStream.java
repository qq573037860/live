package com.sjq.live.utils.media.ffmepg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

class PrintStream extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(PrintStream.class);

    private InputStream __is;

    public PrintStream(InputStream is) {
        __is = is;
    }

    public void run() {
        try {
            for (; ; ) {
                int _ch = __is.read();
                if (_ch == -1) {
                    break;
                } else {
                    System.out.print((char) _ch);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
