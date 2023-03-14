package com.chuhang.framework.exception;

import com.chuhang.framework.model.response.CommonCode;
import com.chuhang.framework.model.response.ResponseResult;
import com.chuhang.framework.model.response.ResultCode;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 异常捕获类
 */
@RestControllerAdvice
public class ExceptionCatch {

    //使用EXCEPTIONS存放异常类型和错误代码的映射，ImmutableMap的特点的一旦创建不可改变，并且线程安全
    private static ImmutableMap<Class<? extends Throwable>,ResultCode> EXCEPTIONS;
    //使用builder来构建一个异常类型和错误代码的异常
    protected static ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder = ImmutableMap.builder();
    static{
        //在这里加入一些基础的异常类型判断
        builder.put(HttpMessageNotReadableException.class, CommonCode.INVALID_PARAM);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);

    /**
     * 捕获指定CustomException异常(用户自定义异常)
     */
    @ExceptionHandler(CustomException.class)
    public ResponseResult customException(CustomException e) {
        LOGGER.error("catch exception : {}\r\nexception: ",e.getMessage(), e);
        ResultCode resultCode = e.getResultCode();
        ResponseResult responseResult = new ResponseResult(resultCode);
        return responseResult;
    }

    /**
     * 捕获指定Exception异常(系统级别)
     */
    @ExceptionHandler(Exception.class)
    public ResponseResult exception(Exception e){
        LOGGER.error("catch exception : {}\r\nexception: ",e.getMessage(), e);

        if(EXCEPTIONS == null){
            EXCEPTIONS = builder.build();
        }

        ResultCode resultCode = EXCEPTIONS.get(e.getClass());

        ResponseResult responseResult;
        if (resultCode != null) {
            responseResult = new ResponseResult(resultCode);
        } else {
            responseResult = new ResponseResult(CommonCode.SERVER_ERROR);
        }
        return responseResult;
    }
}
