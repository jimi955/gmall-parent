package com.atguigu.gmall.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * VO: View Object/Value Object  把从数据库查询出来的数据 封装成 需要传给前端的数据（用户需要的封装到对象中 不需要的不传 和 TO原理相似）
 * request--->提交的vo;
 * response--->返回的vo;
 * <p>
 * DAO: Database Access Object 数据库访问对象 专门对数据库进行crud的对象
 * POJO:  Plain Old Java Object 古老单纯的java对象 JavaBean封装对象用的
 * DO:  Data Object 数据对象 --POJO 数据库对象 专门用来封装数据表的对象
 * TO: Transfer Object 传输对象  aController(user[100个]) == aService.a(user[3个])
 * 用于传输对象（即 用多少 传输多少 节省资源传输量）
 * DTO: Data Transfer Object 也叫 TO
 *
 *  如果导入了一个场景 那么这个场景的第自动配置就会自动生效 我们就必须配置它
 *  但是有时候不需要自动配置生效（比如 导入mybatis但是并不需要连接数据源进行交互数据库 不想配置数据源DataSource）
 *  1）
 *      @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
 *      即使导入了mybatis 也不选要配置其中的数据源
 *  2）
 *      在Pom中排除这个需要配置的依赖
 */



@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GmallAdminWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallAdminWebApplication.class, args);
    }

}
