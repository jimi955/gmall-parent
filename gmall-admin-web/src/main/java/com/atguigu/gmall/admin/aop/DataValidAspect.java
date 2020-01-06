package com.atguigu.gmall.admin.aop;

import com.atguigu.gmall.to.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;


/**
 * 其外面如何写
 * 1:导入切面场景
     * <dependency>
     * <groupId>org.springframework.boot</groupId>
     * <artifactId>spring-boot-starter-aop</artifactId>
     * </dependency>
 * 2:编写切面
     * 1）@Aspect
     * 2) 切入点表达式
     * 3）通知
         * 前置通知
         * 后置通知
         * 最终通知
         * 异常通知
         * 环绕通知
 */

// 利用aop完成统一的数据校验
@Slf4j
@Aspect
@Component
public class DataValidAspect {

    @Pointcut("execution(* com.atguigu.gmall.admin..controller.*.*(..))")  //..*.*表示任意包下任意类的任意方法
    private void pt1() {
    }


    // 根据方法的返回值不同判断  使环绕通知处理
//    @Around("execution(* com.atguigu.gmall.admin..controller.*.*(..))")
    @Around("pt1()")
    public Object validAround(ProceedingJoinPoint point) {
        Object rtValue = null;
        try {
            log.debug("前置");
            Object[] args = point.getArgs();
            for (Object obj : args) {
                if (obj instanceof BindingResult) {
                    BindingResult result = (BindingResult) obj;
                    if (result.getErrorCount() > 0) {
                        // 返回result信息
                        List<FieldError> fieldErrors = result.getFieldErrors();
                        fieldErrors.forEach(fieldError -> {
                            String field = fieldError.getField();
                            log.debug("属性：{} , 传来的值：{} , 校验出错。出错的消息：{}", field, fieldError.getRejectedValue(), fieldError.getDefaultMessage());
                        });
                        return new CommonResult().validateFailed(result);
                    }
                }
            }

            log.debug("开始进入切入点方法");
            rtValue = point.proceed(point.getArgs());//明确调用业务层方法（切入点方法）
            log.debug("后置通知");

            return rtValue;
        } catch (Throwable t) {
            log.debug("异常通知");
            throw new RuntimeException(t);
        } finally {
            log.debug("最终通知");
        }
    }
}
