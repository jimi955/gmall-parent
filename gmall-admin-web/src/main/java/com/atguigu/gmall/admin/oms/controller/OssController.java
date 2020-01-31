package com.atguigu.gmall.admin.oms.controller;


import com.atguigu.gmall.admin.oms.component.OssComponent;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.to.OssPolicyResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


/**
 * Oss相关操作接口
 * 1:阿里云上传
 * 		前端页面form表单文档上传--->后台（收到文件流）---> ossClient.upload()到阿里云
 *
 *
 * 2:如果要配置自己的阿里云
 * 		(1):前端替换掉leifengyan的账号域信息
 * 		(2):application.properties复制成自己的相关值
 * 		(3):开启阿里云的oss跨域访问
 *
 */
//@CrossOrigin(origins = "www.baidu.com")  只允许百度的可以跨域  具体限制参见注解@CrossOrigin
@CrossOrigin
@Controller
@Api(tags = "OssController",description = "Oss管理")
@RequestMapping("/aliyun/oss")
public class OssController {
	@Autowired
	private OssComponent ossComponent;

	@ApiOperation(value = "oss上传签名生成")
	@GetMapping(value = "/policy")
	@ResponseBody
	public Object policy() {
		// 生成签名  签名有效为一次
		OssPolicyResult policy = ossComponent.policy();
		// 将签名返回前端  前端使用签名去上传到阿里云
		return new CommonResult().success(policy);
	}

}
