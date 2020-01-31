package com.atguigu.gmall.locks.controller;

import com.atguigu.gmall.locks.service.RedisIncrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisIncrController {

    @Autowired
    RedisIncrService redisIncrService;

    // 单机下 锁应用
    @GetMapping("/incr")
    public String incr() {
        redisIncrService.incr();
        return "ok";
    }

    // 分布式 锁应用
    @GetMapping("/incr2")
    public String incr2() {
        redisIncrService.incrDistribute();
        return "ok2";
    }

    // 分布式 锁应用
    @GetMapping("/incr3")
    public String incr3() {
        redisIncrService.incrDistribute2();
        return "ok3";
    }
}
