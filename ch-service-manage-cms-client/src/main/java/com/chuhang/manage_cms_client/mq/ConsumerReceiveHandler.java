package com.chuhang.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.chuhang.framework.domain.cms.CmsPage;
import com.chuhang.manage_cms_client.dao.CmsPageRepository;
import com.chuhang.manage_cms_client.service.PageService;
import org.slf4j.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class ConsumerReceiveHandler {

    private static  final Logger LOGGER = LoggerFactory.getLogger(ConsumerReceiveHandler.class);

    @Autowired
    PageService pageService;

    @Autowired
    CmsPageRepository cmsPageRepository;
    /**
     * 接收消息
     * 消息体：
     * {
     *     "pageId":""
     * }
     */
    @RabbitListener(queues = {"${chuhang.mq.queue}"})
    public void postPage(String msg){
        //解析消息
        Map map = JSON.parseObject(msg, Map.class);
        //得到消息中的页面id
        String pageId = (String) map.get("pageId");
        //根据接受到的消息pageId查询出对应页面的信息
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if(!optional.isPresent()){
            LOGGER.error("receive cms post page,cmsPage is null:{}",msg.toString());
            return ;
        }
        //调用service方法将页面从GridFs中下载到服务器
        pageService.savePageToServerPath(pageId);
    }
}
