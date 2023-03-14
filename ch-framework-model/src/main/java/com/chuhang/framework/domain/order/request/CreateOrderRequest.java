package com.chuhang.framework.domain.order.request;

import com.chuhang.framework.model.request.RequestData;
import lombok.Data;
import lombok.ToString;

/**
 * Created by xf on 2018/3/26.
 */
@Data
@ToString
public class CreateOrderRequest extends RequestData {

    String courseId;

}
