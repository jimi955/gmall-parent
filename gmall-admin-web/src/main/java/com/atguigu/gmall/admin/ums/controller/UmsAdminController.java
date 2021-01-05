package com.atguigu.gmall.admin.ums.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.admin.ums.vo.UmsAdminLoginParam;
import com.atguigu.gmall.admin.ums.vo.UmsAdminParam;
import com.atguigu.gmall.admin.utils.JwtTokenUtil;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.ums.entity.Admin;
import com.atguigu.gmall.ums.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台用户管理
 * <p>
 * 如何使用校验：
 * springmvc支持使用JSR303 方式进行校验
 * 1:springboot默认导入了第三方的校验框架 hibernate-validator
 * 使用JSR303三大步：以UmsAdminParam为例
 * 1）：给需要检验数据的javabean上标上注解
 * 2）：告诉SpringBoot 这个需要校验 @Valid
 * springmvc进入方法之前经行校验 如果不成功 不执行方法
 * 3): 如何感知检验失败还是成功：
 * 只要开启了校验的JavaBean的参数后面加一个BindingResult对象就可以获取实验结果
 * 只要有BindingResult 即使校验错了方法也会执行 我们需要手动处理
 * <p>
 * 异常处理
 */
@Slf4j  // 自动生成log对象
@CrossOrigin  // 支持跨域
@RestController
@Api(tags = "AdminController", description = "后台用户管理")
@RequestMapping("/admin")
public class UmsAdminController {
    @Reference
    private AdminService adminService;
    @Value("${gmall.jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${gmall.jwt.tokenHead}")
    private String tokenHead;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @ApiOperation(value = "用户注册")
    @PostMapping(value = "/register")
    public Object register(@Valid @RequestBody UmsAdminParam umsAdminParam, BindingResult result) throws InvocationTargetException, IllegalAccessException {
        // 使用aop切面完成封装数据的检验 见类DataValidAspect

        Admin admin = new Admin();
        admin.setCreateTime(new Date());
        admin.setLoginTime(new Date());
        admin.setStatus(1);
        BeanUtils.copyProperties(umsAdminParam, admin);

        int i = adminService.saveAdmin(admin);
//        int a = 10 / 0;
        if (i>0) {
            log.debug("需要注册用户的详情：{}", umsAdminParam);
            return new CommonResult().success(umsAdminParam);
        }
        return new CommonResult().failed();
    }

    /**
     * 如果前端发送的是json字符串 使用如下封装对象
     * public Object login(@RequestBody UmsAdminLoginParam umsAdminLoginParam, BindingResult result)
     * 如果前端发送的是k=v&k=v字符串 使用如下封装对象
     * public Object login(UmsAdminLoginParam umsAdminLoginParam, BindingResult result)
     *
     * @param
     * @result
     */
    @ApiOperation(value = "登录以后返回token")
    @PostMapping(value = "/login")
    public Object login(@RequestBody UmsAdminLoginParam umsAdminLoginParam, BindingResult result) {
        //去数据库登陆
        Admin admin = adminService.login(umsAdminLoginParam.getUsername(), umsAdminLoginParam.getPassword());
//        Admin admin = null;
        //登陆成功生成token，此token携带基本用户信息，以后就不用去数据库了
        String token = jwtTokenUtil.generateToken(admin);
        if (token == null) {
            return new CommonResult().validateFailed("用户名或密码错误");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);
        return new CommonResult().success(tokenMap);
    }

    @ApiOperation(value = "刷新token")
    @GetMapping(value = "/token/refresh")
    public Object refreshToken(HttpServletRequest request) {
        //1、获取请求头中的Authorization完整值
        String oldToken = request.getHeader(tokenHeader);
        String refreshToken = "";

        //2、从请求头中的Authorization中分离出jwt的值
        String token = oldToken.substring(tokenHead.length());

        //3、是否可以进行刷新（未过刷新时间）
        if (jwtTokenUtil.canRefresh(token)) {
            refreshToken = jwtTokenUtil.refreshToken(token);
        } else if (refreshToken == null && "".equals(refreshToken)) {
            return new CommonResult().failed();
        }

        //将新的token交给前端
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", refreshToken);
        tokenMap.put("tokenHead", tokenHead);
        return new CommonResult().success(tokenMap);
    }

    @ApiOperation(value = "获取当前登录用户信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public Object getAdminInfo(HttpServletRequest request) {
        String oldToken = request.getHeader(tokenHeader);
        String userName = jwtTokenUtil.getUserNameFromToken(oldToken.substring(tokenHead.length()));
        //1.getOne是mybatis-plus生成的 而且带了泛型的
        //2.dubbo没办法直接调用mp中带泛型的service;
        //3.实战经验：
        // mp生成的可能存在兼容问题日 最好不要远程调用
        // Admin umsAdmin = adminService.getOne(new QueryWrapper<Admin>().eq("username", userName));

        Admin umsAdmin = adminService.getUserInfo(userName);
        Map<String, Object> data = new HashMap<>();
        data.put("username", umsAdmin.getUsername());
        data.put("roles", new String[]{"TEST"});
        data.put("icon", umsAdmin.getIcon());
        return new CommonResult().success(data);
    }

    @ApiOperation(value = "登出功能")
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    public Object logout() {
        //TODO 用户退出

        return new CommonResult().success(null);
    }

    @ApiOperation("根据用户名或姓名分页获取用户列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Object list(@RequestParam(value = "name", required = false) String name,
                       @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                       @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        //TODO 分页查询用户信息

        //TODO 响应需要包含分页信息；详细查看swagger规定
        return new CommonResult().failed();
    }

    @ApiOperation("获取指定用户信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Object getItem(@PathVariable Long id) {

        Admin one = adminService.getById(id);

        return new CommonResult().success(one);
    }

    @ApiOperation("更新指定用户信息")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Object update(@PathVariable Long id, @RequestBody Admin admin) {

        //TODO 更新指定用户信息
        return new CommonResult().failed();
    }

    @ApiOperation("删除指定用户信息")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Object delete(@PathVariable Long id) {
        // 对于多个条件使用 或者删除 多条的可以使用 columnMap
//        Map<String, Object> columnMap = new HashMap<>();
//        columnMap.put("email", "test1@163.com");

        boolean b = adminService.removeById(id);
        if (b) {
            return new CommonResult().success("OK");
        }
        return new CommonResult().failed();
    }

    @ApiOperation("给用户分配角色")
    @RequestMapping(value = "/role/update", method = RequestMethod.POST)
    @ResponseBody
    public Object updateRole(@RequestParam("adminId") Long adminId,
                             @RequestParam("roleIds") List<Long> roleIds) {
        //TODO 给用户分配角色
        return new CommonResult().failed();
    }

    @ApiOperation("获取指定用户的角色")
    @RequestMapping(value = "/role/{adminId}", method = RequestMethod.GET)
    @ResponseBody
    public Object getRoleList(@PathVariable Long adminId) {
        //TODO 获取指定用户的角色

        return new CommonResult().success(null);
    }

    @ApiOperation("给用户分配(增减)权限")
    @RequestMapping(value = "/permission/update", method = RequestMethod.POST)
    @ResponseBody
    public Object updatePermission(@RequestParam Long adminId,
                                   @RequestParam("permissionIds") List<Long> permissionIds) {
        //TODO 给用户分配(增减)权限

        return new CommonResult().failed();
    }

    @ApiOperation("获取用户所有权限（包括+-权限）")
    @RequestMapping(value = "/permission/{adminId}", method = RequestMethod.GET)
    @ResponseBody
    public Object getPermissionList(@PathVariable Long adminId) {
        //TODO 获取用户所有权限（包括+-权限）
        return new CommonResult().failed();
    }
}
