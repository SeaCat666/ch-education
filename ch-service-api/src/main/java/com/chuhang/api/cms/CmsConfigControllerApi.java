package com.chuhang.api.cms;

import com.chuhang.framework.domain.cms.CmsConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="cms配置管理接口",tags = "cms配置管理接口，提供数据模型的管理、查询接口")
public interface CmsConfigControllerApi {

    /*
    *根据id获取模型数据信息
    */
    @ApiOperation("根据id查询CMS配置信息")
    CmsConfig getmodel(String id);
}
