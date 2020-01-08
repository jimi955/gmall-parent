package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.entity.Brand;
import com.atguigu.gmall.pms.entity.Product;
import com.atguigu.gmall.pms.service.ProductService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPmsApplicationTests {


    @Autowired
    ProductService productService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedisTemplate<Object, Object> redisTemplateObj;

    @Test
    public void contextLoads() {
        Product product = productService.getById(1);
        System.out.println(product.getName());
    }

    @Test
    public void testRedis() {
//        redisTemplate.opsForValue().append("hello","world");

//        String hello = redisTemplate.opsForValue().get("hello");
//        System.out.println(hello);
//
//        redisTemplateObj.opsForValue().set("obj",new Brand().setName("哈哈哈"));
//
        Brand obj = (Brand)redisTemplateObj.opsForValue().get("obj");
        System.out.println(obj.getName());
    }


}
