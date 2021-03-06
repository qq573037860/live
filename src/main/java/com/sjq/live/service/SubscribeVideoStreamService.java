package com.sjq.live.service;

import com.sjq.live.constant.SubscribeEnum;
import com.sjq.live.controller.TransformStreamManage;
import com.sjq.live.support.AbstractLiveStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Created by shenjq on 2019/12/2
 */

@Component
public class SubscribeVideoStreamService extends AbstractBinaryWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(SubscribeVideoStreamService.class);

    private static final String path = "ws/subscribeVideoStream";

    @Autowired
    private TransformStreamManage manage;

    public SubscribeVideoStreamService() {
        super(path);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        //订阅
        Map<String, Object> map = session.getAttributes();
        String subscribeId = map.get("subscribeId").toString();

        if (null == map.get("registerId")) {//不能重复订阅
            SubscribeEnum subscribeEnum = manage.subscribe(subscribeId, new LiveStreamHandler(session));
            if (0 == subscribeEnum.getCode()) {
                map.put("registerId", subscribeEnum.getName());
            }
        }

        logger.info("client opened: " + session.toString());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Map<String, Object> map = session.getAttributes();
        if (null != map.get("registerId")) {
            String subscribeId = map.get("subscribeId").toString();
            String registerId = map.get("registerId").toString();
            manage.unSubscribe(subscribeId, registerId);
        }

        logger.info("client onclose：" + session.toString());
    }

    class LiveStreamHandler extends AbstractLiveStreamHandler {

        private WebSocketSession session;

        LiveStreamHandler(WebSocketSession session) {
            this.session = session;
        }

        @Override
        public void onData(byte[] bytes) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new BinaryMessage(bytes, true));
                } catch (Exception e) {
                    logger.error("session:" + session.toString() + "，数据发送失败！", e);
                    try {
                        session.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
