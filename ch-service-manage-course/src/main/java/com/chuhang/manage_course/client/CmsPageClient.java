package com.chuhang.manage_course.client;

import com.chuhang.framework.domain.cms.CmsPage;
import com.chuhang.framework.domain.cms.response.CmsPageResult;
import com.chuhang.framework.domain.cms.response.CmsPostPageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "ch-service-manage-cms")
public interface CmsPageClient{
    //保存页面
    @PostMapping("/cms/page/save")
    public CmsPageResult saveOrUpdate(@RequestBody CmsPage cmsPage);

    //课程发布
    @PostMapping("/cms/page/postPageQuick")
    CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage);
}