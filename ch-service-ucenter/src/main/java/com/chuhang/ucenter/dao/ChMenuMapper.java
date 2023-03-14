package com.chuhang.ucenter.dao;

import com.chuhang.framework.domain.ucenter.ChMenu;
import java.util.List;

public interface ChMenuMapper {
    //根据账号查询权限信息
    List<ChMenu> selectPermissionByUserName(String username);
}
