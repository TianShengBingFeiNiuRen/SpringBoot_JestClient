package com.andon.jestclientdemo.controller;

import com.andon.jestclientdemo.domain.KLine;
import com.andon.jestclientdemo.service.ESService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class ESController {

    @Resource
    private ESService esService;

    @GetMapping(value = "/es/search/price")
    public KLine price(String base, String quote, String startTime, String endTime){
        System.out.println("base=" + base + " quote=" + quote + " startTime=" + startTime + " endTime=" + endTime);
        return esService.searchKline(base, quote, startTime, endTime);
    }
}
