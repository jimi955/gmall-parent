package com.atguigu.gmall.search;

import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchApplicationTests {

    @Autowired
    JestClient jestClient;

    @Test
    public void contextLoads() {
    }

    @Test
    public void index() throws IOException {
        User user = new User();
        user.setUsername("xiaoxinxin");
        user.setEmail("1652877739@qq.com");
        Index build = new Index.Builder(user).index("user").type("info").build();

        DocumentResult execute = jestClient.execute(build);
        System.out.println("执行?" + execute.getId() + "==>" + execute.isSucceeded());
    }

}
