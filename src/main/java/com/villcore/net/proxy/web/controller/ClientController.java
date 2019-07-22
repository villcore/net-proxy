package com.villcore.net.proxy.web.controller;

import com.villcore.net.proxy.dns.DNS;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/api/v1/client")
public class ClientController {

    @RequestMapping("/accessablity")
    @ResponseBody
    public Map<String, Boolean> getAllAccessablity() {
        return DNS.getAddressAccessablity();
    }
}
