package com.chuhang.manage_cms.dao;

import com.chuhang.framework.domain.cms.CmsConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CmsConfigRepository extends MongoRepository<CmsConfig,String>{
}
