package com.chuhang.ucenter.dao;

import com.chuhang.framework.domain.ucenter.ChUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChUserRepository extends JpaRepository<ChUser,String> {
    //根据账号查询用户信息
    ChUser findByUsername(String username);
}
