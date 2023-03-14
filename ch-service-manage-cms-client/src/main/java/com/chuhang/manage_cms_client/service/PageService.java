package com.chuhang.manage_cms_client.service;

import com.chuhang.framework.domain.cms.CmsPage;
import com.chuhang.framework.domain.cms.CmsSite;
import com.chuhang.framework.domain.cms.response.CmsCode;
import com.chuhang.framework.exception.ExceptionCast;
import com.chuhang.manage_cms_client.dao.CmsPageRepository;
import com.chuhang.manage_cms_client.dao.CmsSiteRepository;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * 如果接收到了消息，我们定义一个该消息类来调用该类
 * 主要干两个事情
 *  1.需要查询将页面保存的物理路径给干出来
 *      对应页面保存的物理路径=对应站点的物理路径(通过pageId取出对应siteId，然后通过siteId去查询对应的路径)
 *      + 页面的物理路径（通过pageId取出）
 *      + 页面的名称（通过pageId取出）
 *  2.下载GridFS中的html实体页面（只需要cmspaga里面的）
 *      IO流的copy()方法将取到的流干到对应的服务器路径里面去
 */
@Service
public class PageService {
    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsSiteRepository cmsSiteRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    /**
     * 下载gridfs中的html文件并且保存到对应的服务器上去
     */
    public void savePageToServerPath(String pageId){
        //1.根据pageId查询cmspage
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();

        //取出htmlId,通过htmlId去取对应的html文件
        String htmlFileId = cmsPage.getHtmlFileId();
        //获取文件的流
        InputStream inputStream = getFileByHtmlFileId(htmlFileId);
        if (inputStream==null){
            return;
        }
        //2.定义页面的最终保存的物理路径 = 对应站点的物理路径+页面的物理路径+页面的名称
        String pagePath = getCmsSitePhysicalPath(cmsPage.getSiteId()) + cmsPage.getPagePhysicalPath() + cmsPage.getPageName();
        //定义输出流
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(pagePath);
            //3.将html文件保存到对应的服务器上去
            IOUtils.copy(inputStream,fileOutputStream);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据htmlFileId获取GridFS中的文件，并且返回流
     */
    private InputStream getFileByHtmlFileId(String htmlFileId) {
        //文件对象
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(htmlFileId)));
        //打开下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //1.定义GridFsResource，你要告诉GridFS，你需要哪个资源
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        try {
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取站点的物理路径
     */
    public String getCmsSitePhysicalPath(String siteId){
        //根据站点查询站点信息
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if(!optional.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_SITE_NOTEXISTS);
        }
        CmsSite cmsSite = optional.get();
        //获取并返回站点的物理路径
        return cmsSite.getSitePhysicalPath();
    }
}
