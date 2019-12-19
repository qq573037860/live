package com.sjq.live.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by shenjq on 2019/12/17
 */

@Controller
@RequestMapping("/videoChat")
public class VideoChatController {

    @RequestMapping("/view")
    public ModelAndView living() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("videoChat/main");
        return modelAndView;
    }

}
