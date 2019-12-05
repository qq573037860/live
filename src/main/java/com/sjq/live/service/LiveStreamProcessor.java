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

    private static Map<String, Set<WebSocketSession>> subscribeMap = new ConcurrentHashMap<>();

    @Autowired
    private TransformStreamManage manage;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        //订阅
        Map<String, Object> map = session.getAttributes();
        String subscribeId = map.get("subscribeId").toString();

        //记录订阅session
        Set<WebSocketSession> set = subscribeMap.computeIfAbsent(subscribeId, k -> new ConcurrentSkipListSet<>());
        if (set.size() == 0) {//同一个主题只用订阅一次
            manage.subscribe(subscribeId, new ReadHandlerImpl(subscribeId));
        }
        set.add(session);

        logger.info("client opened: " + session.toString());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Iterator<Set<WebSocketSession>> setIterator = subscribeMap.values().iterator();
        while (setIterator.hasNext()) {
            Set<WebSocketSession> set = setIterator.next();
            Iterator<WebSocketSession> it = set.iterator();
            while (it.hasNext()) {
                WebSocketSession webSocketSession = it.next();
                if (webSocketSession.getId().equals(session.getId())) {
                    it.remove();
                }
            }
            if (set.size() == 0) {
                setIterator.remove();
            }
        }
        logger.info("client onclose：" + session.toString());
    }

    class ReadHandlerImpl extends ReadHandler {

        private String subscribeId;

        ReadHandlerImpl(String subscribeId) {
            this.subscribeId = subscribeId;
        }

        @Override
        public void read(byte[] bytes) {
            Set<WebSocketSession> set = subscribeMap.get(subscribeId);
            Iterator<WebSocketSession> it = set.iterator();
            while (it.hasNext()) {
                WebSocketSession session = it.next();
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
}
