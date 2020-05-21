package com.sjq.live.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by shenjq on 2019/12/17
 */

@Controller
@RequestMapping(value = "/videoChat")
public class VideoChatController {

    @RequestMapping(value = "/view")
    public String living() {
        return "videoChat/main.html";
    }

}
