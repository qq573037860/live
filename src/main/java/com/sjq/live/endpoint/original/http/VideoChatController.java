package com.sjq.live.endpoint.original.http;

import com.sjq.live.endpoint.original.OriginalEndPointSwitch;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by shenjq on 2019/12/17
 */

@ConditionalOnBean(OriginalEndPointSwitch.class)
@Controller
@RequestMapping(value = "/videoChat")
public class VideoChatController {

    @RequestMapping(value = "/view")
    public String living() {
        return "videoChat/main.html";
    }

}
