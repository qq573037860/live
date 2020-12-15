package com.sjq.live.utils.ffmepg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OutHandler extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(OutHandler.class);

    // 控制线程状态
    volatile boolean status = true;

    BufferedReader br;

    String type;

    public OutHandler(InputStream is, String type) {
        br = new BufferedReader(new InputStreamReader(is));
        this.type = type;
    }

    /**
     * 重写线程销毁方法，安全的关闭线程
     */
    @Override
    public void destroy() {
        status = false;
    }

    /**
     * 执行输出线程
     */
    @Override
    public void run() {
        String msg;
        try {
            while (status) {
                if ((msg = br.readLine()) != null) {
                    logger.info("{}消息：{}", type, msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
