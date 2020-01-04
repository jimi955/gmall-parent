package com.atguigu.gmall.ums;

import com.atguigu.gmall.ums.entity.Role;
import com.atguigu.gmall.ums.service.RoleService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallUmsApplicationTests {

    @Autowired
    RoleService roleService;

    @Test
    public void contextLoads() {
        Role role = roleService.getById(1);
        System.out.println(role.getName());

    }

}
