package com.chuhang.auth.client;

import com.chuhang.framework.client.ChServiceList;
import com.chuhang.framework.domain.ucenter.ext.ChUserExt;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = ChServiceList.CH_SERVICE_UCENTER)
public interface UserClient {
    //根据账号查询用户信息
    @GetMapping("/ucenter/getuserext")
    public ChUserExt getUserext(@RequestParam("username") String username);
}
