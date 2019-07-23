package com.villcore.net.proxy.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * created by WangTao on 2019-07-23
 */
@Controller
public class IndexController {
    @RequestMapping(value = {"/index", "/"})
    public String index() {
        System.out.println(new Date());
        return "templates/index.html";
    }
}
