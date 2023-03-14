package com.chuhang.framework.domain.cms.request;

import lombok.Data;

/**
 * Created By xf on 2019/9/11
 * CmsPage查询对象，方便以后扩展查询条件
 */
@Data
public class QueryPageRequest {
    //站点id
    private String siteId;
    //页面ID
    private String pageId;
    //页面名称
    private String pageName;
    //别名
    private String pageAliase;
    //模版id
    private String templateId;
}
