package com.chuhang.framework.domain.cms.response;

import com.chuhang.framework.model.response.ResponseResult;
import com.chuhang.framework.model.response.ResultCode;
import lombok.Data;

/**
 * Created by xf on 2018/3/31.
 */
@Data
public class GenerateHtmlResult extends ResponseResult {
    String html;
    public GenerateHtmlResult(ResultCode resultCode, String html) {
        super(resultCode);
        this.html = html;
    }
}
