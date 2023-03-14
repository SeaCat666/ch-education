package com.chuhang.manage_cms.controller;

import com.chuhang.framework.web.BaseController;
import com.chuhang.manage_cms.service.CmsPageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class CmsPagePreviewController extends BaseController {

    @Autowired
    CmsPageService cmsPageService;

    @GetMapping(value = "/cms/preview/{pageId}")
    public void preview(@PathVariable("pageId") String pageId){
        //调用静态化程序生成静态页面内容
        String pageHtml = cmsPageService.getPageHtml(pageId);
        if (StringUtils.isNotEmpty(pageHtml)){
            try {
                ServletOutputStream servletOutputStream=response.getOutputStream();
                response.setContentType("text/html;charset=utf-8");
                //将静态化后的结果页面完整输入到浏览器，从而形成页面
                servletOutputStream.write(pageHtml.getBytes("utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
