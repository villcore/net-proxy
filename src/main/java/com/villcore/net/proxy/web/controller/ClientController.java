package com.villcore.net.proxy.web.controller;

import com.villcore.net.proxy.dns.DNS;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/api/v1/client")
public class ClientController {

    @RequestMapping(value = "/accessablity", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Boolean> getAllAccessablity() {
        return DNS.getAddressAccessablity();
    }

    @RequestMapping(value = "/accessablity/update", method = RequestMethod.GET)
    @ResponseBody
    public void updateAllAccessablity(
            @RequestParam(name = "address") String address,
            @RequestParam(name = "accessablity") boolean accessablity) {
        DNS.updateAccessablity(address, accessablity);
    }
}
