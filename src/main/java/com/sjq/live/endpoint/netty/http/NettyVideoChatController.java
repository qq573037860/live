package com.sjq.live.endpoint.netty.http;

import com.sjq.live.endpoint.netty.NettyEndPointSwitch;
import com.sjq.live.endpoint.netty.bootstrap.NettyEndPoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by shenjq on 2019/12/17
 */

@NettyEndPoint(path = "/videoChat")
@ConditionalOnBean(NettyEndPointSwitch.class)
@Component
public class NettyVideoChatController {

    @NettyEndPoint(path = "/view")
    public String living() {
        return "templates/videoChat/main.html";
    }

}
