package com.chuhang.ucenter.controller;

import com.chuhang.api.ucenter.UcenterControllerApi;
import com.chuhang.framework.domain.ucenter.ext.ChUserExt;
import com.chuhang.ucenter.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ucenter")
public class UcenterController implements UcenterControllerApi {
    @Autowired
    UserService userService;

    @Override
    @GetMapping("/getuserext")
    public ChUserExt getUserext(@RequestParam("username") String username) {
        return userService.getUserExt(username);
    }
}
