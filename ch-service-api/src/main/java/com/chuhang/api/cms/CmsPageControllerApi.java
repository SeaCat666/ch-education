package com.chuhang.api.cms;

import com.chuhang.framework.domain.cms.CmsPage;
import com.chuhang.framework.domain.cms.request.QueryPageRequest;
import com.chuhang.framework.domain.cms.response.CmsPageResult;
import com.chuhang.framework.domain.cms.response.CmsPostPageResult;
import com.chuhang.framework.model.response.QueryResponseResult;
import com.chuhang.framework.model.response.ResponseResult;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.PathVariable;

@Api(value = "cms页面管理接口",tags = {"cms页面管理接口，提供页面的CRUD"})
public interface CmsPageControllerApi {
    /**
     * 查询页面
     * @param page 当前页
     * @param size 每页显示多少条
     * @param queryPageRequest 查询对象
     * @return 统一的响应返回对象
     */
    @ApiOperation("分页查询页面列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",paramType = "path",dataType ="int",required = true),
            @ApiImplicitParam(name = "size",value = "每页条数",paramType = "path",dataType ="int",required = true),
    })
    QueryResponseResult findList(@PathVariable("page") int page,
                                 @PathVariable("size") int size,
                                 QueryPageRequest queryPageRequest);
    /**
     * 添加页面
     * @param cmsPage 页面对象
     * @return 带状态码，添加的对象信息的CmsPageResult
     */
    @ApiOperation("添加页面")
    CmsPageResult add(CmsPage cmsPage);

    /**
     * 添加页面
     * @param id 页面id
     * @return 带状态码，添加的对象信息的CmsPageResult
     */
    @ApiOperation("通过ID查询页面")
    CmsPage findById(String id);

    /**
     * 添加页面
     * @param id 页面id，CmsPage 页面对象
     * @return 带状态码，添加的对象信息的CmsPageResult
     */
    @ApiOperation("编辑页面")
    CmsPageResult edit(String id,CmsPage cmsPage);

    /**
     * 添加页面
     * @param id 页面id，CmsPage 页面对象
     * @return 带状态码，添加的对象信息的CmsPageResult
     */
    @ApiOperation("通过ID删除页面")
    ResponseResult delete(String id);

   /**
    *
    *页面发布
    */
   @ApiOperation("页面发布")
    ResponseResult post(String pageId);

   /**
    *public保存页面,比如一些需要页面预览时没有pageId,所以给他们添加一个新的页面生产新的id
    */
   @ApiOperation("保存页面")
    CmsPageResult saveOrUpdate(CmsPage cmsPage);

   /**
    *一键发布
    */
   @ApiOperation("一键发布页面")
    CmsPostPageResult postPageQuick(CmsPage cmsPage);

}
