package com.chuhang.manage_cms.service;

import com.chuhang.framework.domain.cms.CmsConfig;
import com.chuhang.manage_cms.dao.CmsConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CmsConfigService {
    @Autowired
    CmsConfigRepository cmsConfigRepository;

    //根据id查询配置管理信息
    public CmsConfig findById(String id){
        Optional<CmsConfig> optional=cmsConfigRepository.findById(id);
        if (optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }
}
