package com.chuhang.manage_course.service;

import com.chuhang.framework.domain.system.SysDictionary;
import com.chuhang.manage_course.dao.SysDictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysDictionaryService {
    @Autowired
    SysDictionaryRepository sysDictionaryRepository;

    //根据字典分类type查询字典信息
    public SysDictionary findBydType(String type){
        SysDictionary sysDictionary = sysDictionaryRepository.findBydType(type);
        return sysDictionary;
    }
}
