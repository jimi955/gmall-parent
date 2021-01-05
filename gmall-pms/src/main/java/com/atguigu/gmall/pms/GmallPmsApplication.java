package com.atguigu.gmall.pms;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
    1:配置整合Dubbo
    2:配置整合MybatisPlus
    logstash整合
        1）导入jar包  https://mvnrepository.com/artifact/net.logstash.logback/logstash-logback-encoder
        2）导入日志配置  logback-spring.xml
        3) 建立好日志的索引

    使用缓存（对于使用频繁的查询语句 返回的结构推荐使用缓存）
        redisTemplate  StringRedisTemplate
    设计模式：模板模式
        redisTemplate MongoTemplate JdbcTemplate RestTemplate(spring_cloud)

    整合redis两大部：
        1：引入redis starter
        2: 配置redis地址

 *  事务的最终解决方案:
 *      1)普通的事务 导入jdbc-starter @EnableTransactionManagement 加@Transaction
 *      2)方法自己调用自己类里面的 加不上事务
 *          1)导入starter-aop 在主类上开启@EnableAspectJAutoProxy(exposeProxy = true): 暴露代理对象
 *          2)获取当前类的真正代理对象

 */

@EnableDubbo
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.atguigu.gmall.pms.mapper")
//@ComponentScan
@SpringBootApplication
public class GmallPmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPmsApplication.class, args);
    }

}
