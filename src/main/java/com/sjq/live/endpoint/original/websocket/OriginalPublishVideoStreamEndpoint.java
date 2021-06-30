package com.sjq.live.endpoint.original.websocket;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.endpoint.original.OriginalEndPointSwitch;
import com.sjq.live.handler.VideoStreamHandler;
import com.sjq.live.model.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

/**
 * Created by shenjq on 2019/12/2
 */

@Component
@ConditionalOnBean(OriginalEndPointSwitch.class)
public class OriginalPublishVideoStreamEndpoint extends AbstractBinaryWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(OriginalPublishVideoStreamEndpoint.class);

    @Autowired
    @Qualifier("defaultPublishVideoStreamHandler")
    private VideoStreamHandler videoStreamHandler;

    public OriginalPublishVideoStreamEndpoint() {
        super(LiveConfiguration.PUBLISH_PATH);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("client opened: " + session.toString());
        videoStreamHandler.afterConnectionEstablished(new RequestParam(session.getAttributes()));
        //handleBinaryMessage(session, null);
    }

    /**
     * 主要用来接受二进制数据(视频流)。
     * @param session
     * @param message
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        videoStreamHandler.handleBinaryMessage(new RequestParam(session.getAttributes()), message.getPayload().array());

        /*FileInputStream in;
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
        }*/


    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        videoStreamHandler.afterConnectionClosed(new RequestParam(session.getAttributes()));
        logger.info("client onclose[session={},status={}]", session, status);
    }
}
