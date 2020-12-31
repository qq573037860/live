package com.sjq.live.service;

import com.sjq.live.model.OperateResponse;
import com.sjq.live.support.PublishHandler;
import com.sjq.live.model.WebSocketAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

/**
 * Created by shenjq on 2019/12/2
 */

@Component
public class PublishVideoStreamEndpoint extends AbstractBinaryWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(PublishVideoStreamEndpoint.class);

    private static final String path = "ws/publishVideoStream";

    @Autowired
    private TransformStream transformStream;

    public PublishVideoStreamEndpoint() {
        super(path);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("client opened: " + session.toString());

        final WebSocketAttribute<Object, String> attribute = new WebSocketAttribute<>(session.getAttributes());
        //获取写入流句柄
        final OperateResponse<PublishHandler> operateResponse = transformStream.publish(attribute.getPublishId());
        if (operateResponse.isSuccess()) {
            attribute.setPublishHandler(operateResponse.getData());
        }

        handleBinaryMessage(session, null);
    }

    /**
     * 主要用来接受二进制数据(视频流)。
     * @param session
     * @param message
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        final WebSocketAttribute<Object, PublishHandler> attribute = new WebSocketAttribute<>(session.getAttributes());
        //向管道中写入数据
        final PublishHandler writeHandler = attribute.getPublishHandler();

        FileInputStream in;
        try {
            in = new FileInputStream("D:\\BaiduNetdiskDownload\\01Python快速入门\\b_edit.mp4");
            byte[] bytes = new byte[1024*8];
            int len = 0;
            while (true) {
                try {
                    if ((len = in.read(bytes)) == -1) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //handleBinaryMessage(session, new BinaryMessage(bytes, 0, len, true));
                byte[] b = new byte[len];
                System.arraycopy(bytes, 0, b, 0, len);
                writeHandler.write(b);
            }
            in.close();
            writeHandler.write(new byte[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //writeHandler.write(message.getPayload().array());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        final WebSocketAttribute<Object, PublishHandler> attribute = new WebSocketAttribute<>(session.getAttributes());
        //关闭管道
        PublishHandler handler = attribute.getPublishHandler();
        if (Objects.nonNull(handler)) {
            handler.close();
        }
        logger.info("client onclose：" + session.toString());
    }
}
