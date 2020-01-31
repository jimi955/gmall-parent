package com.atguigu.gmall.admin.ums.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.ums.entity.MemberLevel;
import com.atguigu.gmall.ums.service.MemberLevelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@Slf4j
@CrossOrigin
@RestController
@Api(tags = "UmsMemberLevelController", description = "会员等级查询")
@RequestMapping(value ="/memberLevel")
public class UmsMemberLevelController {

    @Reference
    MemberLevelService service;
    /**
     * 查出会员等级
     */
    @ApiOperation(value="返回会员等级列表")
    @GetMapping(value="/list")
    public Object memberLevelList(){
        List<MemberLevel> list = service.list();
        return new CommonResult().success(list);
    }
}
