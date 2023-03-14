package com.chuhang.ucenter.dao;

import com.chuhang.framework.domain.ucenter.ChCompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChCompanyUserRepository extends JpaRepository<ChCompanyUser,String> {
    //根据用户id查询该用户所属的公司id
    ChCompanyUser findByUserId(String userId);
}
