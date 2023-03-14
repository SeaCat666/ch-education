package com.chuhang.manage_course.exception;

import com.chuhang.framework.exception.ExceptionCatch;
import com.chuhang.framework.model.response.CommonCode;
import org.springframework.security.access.AccessDeniedException;//*bug*
import org.springframework.web.bind.annotation.ControllerAdvice;


@ControllerAdvice//控制器增强
public class CustomExceptionCatch extends ExceptionCatch {
    static {
        //除了CustomException以外的异常类型及对应的错误代码在这里定义,，如果不定义则统一返回固定的错误信息
        builder.put(AccessDeniedException.class, CommonCode.UNAUTHORISE);
    }
}
