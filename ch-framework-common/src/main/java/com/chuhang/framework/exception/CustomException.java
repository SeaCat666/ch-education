package com.chuhang.framework.exception;

import com.chuhang.framework.model.response.ResultCode;
/**
 * 自定义异常类
 */
public class CustomException extends RuntimeException {
    //异常信息：状态码+错误信息
    private ResultCode resultCode;
    //添加构造
    public CustomException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }
    //暴漏获取错误状态的get方法，后面异常捕获类需要拿到
    public ResultCode getResultCode(){
        return this.resultCode;
    }
}
