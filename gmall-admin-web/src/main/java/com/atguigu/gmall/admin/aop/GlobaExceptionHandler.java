package com.atguigu.gmall.admin.aop;

import com.atguigu.gmall.to.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理组件
 */
@Slf4j
@RestControllerAdvice
public class GlobaExceptionHandler {

    // ArithmeticException 数学运算异常
    @ExceptionHandler(value={ArithmeticException.class})
    public Object handleException(Exception e){
        log.debug("数学运算异常感知，信息：{}",e.getStackTrace());
        return new CommonResult().validateFailed("数学没学好---");
    }

    @ExceptionHandler(value={NullPointerException.class})
    public Object handleException02(Exception e){
        log.debug("空指针异常感知，信息：{}",e.getStackTrace());
        return new CommonResult().validateFailed("空指针异常---");
    }
}
