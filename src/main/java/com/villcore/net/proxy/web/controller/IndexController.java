package com.villcore.net.proxy.web.controller;

import com.villcore.net.proxy.dns.DNS;
import com.villcore.net.proxy.metric.ClientMetrics;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * created by WangTao on 2019-07-23
 */
@RestController
public class IndexController {
    @RequestMapping(value = {"/index", "/"})
    public ModelAndView index() {
        return new ModelAndView("client_management")
                .addObject("accessablityMap", getAllAccessablity())
                .addObject("globalProxy", DNS.isGlobalProxy())
                .addObject("localChannels", ClientMetrics.openLocalChannels())
                .addObject("remoteChannels", ClientMetrics.openRemoteChannels())
                .addObject("rx", getByteUnit(ClientMetrics.rx()))
                .addObject("rxTotal", getByteUnit(ClientMetrics.rxTotal()))
                .addObject("tx", getByteUnit(ClientMetrics.tx()))
                .addObject("txTotal", getByteUnit(ClientMetrics.txTotal()))
                .addObject("openChannels", ClientMetrics.openChanels());
    }

    public String getByteUnit(long byteLen) {
        if (byteLen < 1024L) { //kb
            return byteLen + "b";
        }

        if (byteLen < 1024L * 1024L) { //mb
            return String.format("%.1fKb", (double) (byteLen / 1024.0));
        }

        if (byteLen < 1024L * 1024L * 1024L) { //gb
            return String.format("%.1fMb", (double) (byteLen / (1024.0 * 1024.0)));
        }

        if (byteLen < (1024L * 1024L * 1024L * 1024L)) { //tb
            return String.format("%.1fGb", (double) (byteLen / (1024.0 * 1024.0 * 1024.0)));
        }

        return byteLen + "b";
    }

    @RequestMapping(value = "/accessablity", method = RequestMethod.GET)
    @ResponseBody
    public List<DNS.Accessiblity> getAllAccessablity() {

        List<DNS.Accessiblity> accessiblityList = new ArrayList<>(DNS.getAddressAccessablity().values().size());
        DNS.getAddressAccessablity().values().forEach(v -> {
            accessiblityList.add(new DNS.Accessiblity(v.getAddress(), v.isAccessable(), v.getCount()));
        });

        accessiblityList.sort(new Comparator<DNS.Accessiblity>() {
            @Override
            public int compare(DNS.Accessiblity o1, DNS.Accessiblity o2) {
                return o2.getCount() - o1.getCount();
            }
        });

        accessiblityList.removeIf(accessiblity -> accessiblity.getCount() == 0);
        return accessiblityList;
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
