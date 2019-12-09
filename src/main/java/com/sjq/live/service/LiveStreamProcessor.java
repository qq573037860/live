package com.sjq.live.service;

import com.sjq.live.controller.TransformStreamManage;
import com.sjq.live.controller.TransformStreamManage.ReadHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by shenjq on 2019/12/2
 */

@Component
public class LiveStreamProcessor extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(LiveStreamProcessor.class);

    @Autowired
    private TransformStreamManage manage;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        //订阅
        Map<String, Object> map = session.getAttributes();
        String subscribeId = map.get("subscribeId").toString();
        String registerId = map.get("registerId").toString();

        if (null == registerId) {//不能重复订阅
            map.put("registerId", manage.subscribe(subscribeId, new ReadHandlerImpl(session)).getName());
        }

        logger.info("client opened: " + session.toString());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Map<String, Object> map = session.getAttributes();
        String subscribeId = map.get("subscribeId").toString();
        String registerId = map.get("registerId").toString();
        manage.unSubscribe(subscribeId, registerId);

        logger.info("client onclose：" + session.toString());
    }

    class ReadHandlerImpl extends ReadHandler {

        private WebSocketSession session;

        ReadHandlerImpl(WebSocketSession session) {
            this.session = session;
        }

        @Override
        public void read(byte[] bytes) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new BinaryMessage(bytes));
                } catch (IOException e) {
                    logger.error("session:" + session.toString() + "，数据发送失败！", e);
                }
            }
        }
    }
}
