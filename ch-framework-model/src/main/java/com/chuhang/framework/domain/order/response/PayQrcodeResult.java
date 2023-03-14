package com.chuhang.framework.domain.order.response;

import com.chuhang.framework.model.response.ResponseResult;
import com.chuhang.framework.model.response.ResultCode;
import lombok.Data;
import lombok.ToString;

/**
 * Created by xf on 2018/3/27.
 */
@Data
@ToString
public class PayQrcodeResult extends ResponseResult {
    public PayQrcodeResult(ResultCode resultCode){
        super(resultCode);
    }
    private String codeUrl;
    private Float money;
    private String orderNumber;

}
