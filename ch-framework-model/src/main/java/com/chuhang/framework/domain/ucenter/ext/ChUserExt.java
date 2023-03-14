package com.chuhang.framework.domain.ucenter.ext;

import com.chuhang.framework.domain.ucenter.ChMenu;
import com.chuhang.framework.domain.ucenter.ChUser;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Created by admin on 2018/3/20.
 */
@Data
@ToString
public class ChUserExt extends ChUser {

    //权限信息
    private List<ChMenu> permissions;

    //企业信息
    private String companyId;
}
