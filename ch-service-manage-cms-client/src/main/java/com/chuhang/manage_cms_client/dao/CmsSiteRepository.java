package com.chuhang.manage_cms_client.dao;

import com.chuhang.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * 查询站点的信息:就是为了通过站点的id拿到对应站点的物理路径
 */
public interface CmsSiteRepository extends MongoRepository<CmsSite,String> {

}