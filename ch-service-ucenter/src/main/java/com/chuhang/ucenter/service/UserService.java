package com.chuhang.ucenter.service;

import com.chuhang.framework.domain.ucenter.ChCompanyUser;
import com.chuhang.framework.domain.ucenter.ChMenu;
import com.chuhang.framework.domain.ucenter.ChUser;
import com.chuhang.framework.domain.ucenter.ext.ChUserExt;
import com.chuhang.ucenter.dao.ChCompanyUserRepository;
import com.chuhang.ucenter.dao.ChMenuMapper;
import com.chuhang.ucenter.dao.ChUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    ChUserRepository chUserRepository;
    @Autowired
    ChCompanyUserRepository chCompanyUserRepository;
    @Autowired
    ChMenuMapper chMenuMapper;

    //根据账号查询用户信息
    public ChUserExt getUserExt(String username){
        //根据账号查询chUser信息
        ChUser chUser = this.findChUserByUsername(username);
        if (chUser==null){
            return null;
        }
        //获取用户的 id
        String userId = chUser.getId();

        //根据 userId 查询用户所属公司信息
        ChCompanyUser chCompanyUser = chCompanyUserRepository.findByUserId(userId);
        //取到用户的公司id
        String companyId=null;
        if (chCompanyUser!=null){
            companyId=chCompanyUser.getCompanyId();
        }

        //根据用户名查询权限信息
        List<ChMenu> chMenus = chMenuMapper.selectPermissionByUserName(username);
        if (chMenus==null) {
            return null;
        }

        ChUserExt chUserExt=new ChUserExt();
        BeanUtils.copyProperties(chUser,chUserExt);
        chUserExt.setPermissions(chMenus);
        chUserExt.setCompanyId(companyId);
        return chUserExt;
    }

    //根据账号查询chUser信息
    private ChUser findChUserByUsername(String username){
        return chUserRepository.findByUsername(username);
    }
}
