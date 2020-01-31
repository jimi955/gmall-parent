package com.atguigu.es.esdemo;

import com.atguigu.es.esdemo.user.User;
import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
class EsDemoApplicationTests {

    @Autowired
    JestClient jestClient;

    @Test
    void contextLoads() {
        System.out.println(jestClient);
    }

    @Test
    void index() throws IOException {
        User user = new User();
        user.setUsername("xiaoxin");
        user.setEmail("1652877739@qq.com");
        Index build = new Index.Builder(user).index("user").type("info").build();

        DocumentResult execute = jestClient.execute(build);
        System.out.println("执行?"+execute.getId()+"==>"+execute.isSucceeded());
    }

}
