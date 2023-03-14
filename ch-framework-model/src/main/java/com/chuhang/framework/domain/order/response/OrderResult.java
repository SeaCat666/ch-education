package com.chuhang.framework.domain.order.response;

import com.chuhang.framework.domain.order.ChOrders;
import com.chuhang.framework.model.response.ResponseResult;
import com.chuhang.framework.model.response.ResultCode;
import lombok.Data;
import lombok.ToString;

/**
 * Created by xf on 2018/3/26.
 */
@Data
@ToString
public class OrderResult extends ResponseResult {
    private ChOrders chOrders;
    public OrderResult(ResultCode resultCode, ChOrders chOrders) {
        super(resultCode);
        this.chOrders = chOrders;
    }


}
