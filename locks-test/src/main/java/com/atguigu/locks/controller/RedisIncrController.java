package com.atguigu.locks.controller;

import com.atguigu.locks.service.RedisIncrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class RedisIncrController {

    @Value("${server.port}")
    String port;

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

    // Redission 分布式 锁应用
    @GetMapping("/incr4")
    public String incr4(HttpServletRequest request) {
        String num = redisIncrService.incrDistribute4();
        return "port: " + port + " result: " + num;
    }


    @GetMapping("/read")
    public String read() {
        return redisIncrService.read();
    }

    @GetMapping("/write")
    public String write() throws InterruptedException {
        return redisIncrService.write();
    }
}
