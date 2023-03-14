package com.chuhang.framework.domain.order.response;

import com.chuhang.framework.domain.order.ChOrdersPay;
import com.chuhang.framework.model.response.ResponseResult;
import com.chuhang.framework.model.response.ResultCode;
import lombok.Data;
import lombok.ToString;

/**
 * Created by xf on 2018/3/27.
 */
@Data
@ToString
public class PayOrderResult extends ResponseResult {
    public PayOrderResult(ResultCode resultCode) {
        super(resultCode);
    }
    public PayOrderResult(ResultCode resultCode, ChOrdersPay chOrdersPay) {
        super(resultCode);
        this.chOrdersPay = chOrdersPay;
    }
    private ChOrdersPay chOrdersPay;
    private String orderNumber;

    //当tradeState为NOTPAY（未支付）时显示支付二维码
    private String codeUrl;
    private Float money;


}
