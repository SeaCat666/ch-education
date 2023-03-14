package com.chuhang.manage_cms.controller;

import com.chuhang.api.cms.CmsPageControllerApi;
import com.chuhang.framework.domain.cms.CmsPage;
import com.chuhang.framework.domain.cms.request.QueryPageRequest;
import com.chuhang.framework.domain.cms.response.CmsPageResult;
import com.chuhang.framework.domain.cms.response.CmsPostPageResult;
import com.chuhang.framework.model.response.QueryResponseResult;
import com.chuhang.framework.model.response.ResponseResult;
import com.chuhang.manage_cms.service.CmsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cms/page")
public class CmsPageController implements CmsPageControllerApi {

    @Autowired
    CmsPageService cmsPageService;


    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page,
                                        @PathVariable("size") int size,
                                        QueryPageRequest queryPageRequest) {
        /*//构建返回对象里需要的集合
        List<CmsPage> cmsPageList=new ArrayList<>();
        CmsPage cmsPage=new CmsPage();
        cmsPage.setPageName("测试页面");
        cmsPageList.add(cmsPage);

        //先暂时用静态数据
        QueryResult<CmsPage> queryResult=new QueryResult<>();
        //设置返回的集合和分页条
        queryResult.setList(cmsPageList);
        queryResult.setTotal(10);*/

        QueryResponseResult result = cmsPageService.findList(page, size, queryPageRequest);
        //构建返回到前台的对象
        return result;
    }

    @Override
    @PostMapping("/add")
    public CmsPageResult add(@RequestBody CmsPage cmsPage) {
        return cmsPageService.add(cmsPage);
    }


    @Override
    @GetMapping("/findById/{id}")
    public CmsPage findById(@PathVariable("id") String id) {
        return cmsPageService.getById(id);
    }

    @Override
    @PutMapping("/edit/{id}")
    public CmsPageResult edit(@PathVariable("id") String id,
                              @RequestBody CmsPage cmsPage)
    {
        return cmsPageService.edit(id,cmsPage);
    }

    @DeleteMapping("/delete/{id}")
    @Override
    public ResponseResult delete(@PathVariable("id") String id) {
        return cmsPageService.delete(id);
    }


    /**
     * 发布页面方法
     * 不需要返回数据就直接返回ResponseResult
     * 如果需要数据返回就自定义一个Result去继承ResponseResult
     */
    @PostMapping("/postPage/{id}")
    @Override
    public ResponseResult post(@PathVariable("id") String pageId) {
        return cmsPageService.postPage(pageId);
    }

    /**
     *public保存页面
     */
    @PostMapping("/save")
    @Override
    public CmsPageResult saveOrUpdate(@RequestBody CmsPage cmsPage) {
        return cmsPageService.saveOrUpdate(cmsPage);
    }

    /**
     *一键发布
     */
    @PostMapping("/postPageQuick")
    @Override
    public CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage) {
        return cmsPageService.postPageQuick(cmsPage);
    }
}
