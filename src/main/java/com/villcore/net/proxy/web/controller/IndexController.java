package com.villcore.net.proxy.web.controller;

import com.villcore.net.proxy.dns.DNS;
import com.villcore.net.proxy.metric.ClientMetrics;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * created by WangTao on 2019-07-23
 */
@RestController
public class IndexController {
    @RequestMapping(value = {"/index", "/"})
    public ModelAndView index() {
        return new ModelAndView("client_management")
                .addObject("accessablityMap", DNS.getAddressAccessablity())
                .addObject("globalProxy", DNS.isGlobalProxy())
                .addObject("localChannels", ClientMetrics.openLocalChannels())
                .addObject("remoteChannels", ClientMetrics.openRemoteChannels())
                .addObject("openChannels", ClientMetrics.openChanels());
    }

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

    @RequestMapping(value = "/global_proxy", method = RequestMethod.GET)
    public ModelAndView setGlobalProxy(
            @RequestParam(name = "globalProxy") Boolean globalProxy) {
        DNS.setGlobalProxy(globalProxy);
        return index();
    }

    @RequestMapping(value = "/proxy", method = RequestMethod.GET)
    public ModelAndView proxy(
            @RequestParam(name = "address") String address) {
        DNS.updateAccessablity(address, false);
        return index();
    }

    @RequestMapping(value = "/local", method = RequestMethod.GET)
    public ModelAndView local(
            @RequestParam(name = "address") String address) {
        DNS.updateAccessablity(address, true);
        return index();
    }

    @RequestMapping(value = "/remove", method = RequestMethod.GET)
    public ModelAndView remove(
            @RequestParam(name = "address") String address) {
        DNS.removeAccessablity(address);
        return index();
    }

    @RequestMapping(value = "/metrics", method = RequestMethod.GET)
    public ModelAndView metrics(
            @RequestParam(name = "address") String address) {
        return index();
    }
}