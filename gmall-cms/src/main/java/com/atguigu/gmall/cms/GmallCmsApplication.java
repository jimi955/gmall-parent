package com.atguigu.gmall.cms;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 1:配置整合Dubbo
 2:配置整合MybatisPlus
 logstash整合
 1）导入jar包  https://mvnrepository.com/artifact/net.logstash.logback/logstash-logback-encoder
 2）导入日志配置  logback-spring.xml
 3) 建立好日志的索引

 */

@EnableDubbo
@MapperScan("com.atguigu.gmall.cms.mapper")
@SpringBootApplication
public class GmallCmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallCmsApplication.class, args);
    }

}
