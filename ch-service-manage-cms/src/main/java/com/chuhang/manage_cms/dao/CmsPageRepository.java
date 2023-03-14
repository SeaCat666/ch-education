package com.chuhang.manage_cms.dao;

import com.chuhang.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * springboot里使用MongDB操作非常简单，只要继承MongoRepository
 * 指定对应的实体对象和主键类型就可以了
 */
public interface CmsPageRepository extends MongoRepository<CmsPage,String> {
    //根据页面名称查询
    CmsPage findByPageName(String pageName);

    //根据 页面名称、站点`Id`、页面`webpath`查询数据库添加时是否有相同数据
    CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String siteId,String pageWebPath);
}
