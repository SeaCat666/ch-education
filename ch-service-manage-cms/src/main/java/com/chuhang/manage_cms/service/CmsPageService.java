package com.chuhang.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.chuhang.framework.domain.cms.CmsPage;
import com.chuhang.framework.domain.cms.CmsSite;
import com.chuhang.framework.domain.cms.CmsTemplate;
import com.chuhang.framework.domain.cms.request.QueryPageRequest;
import com.chuhang.framework.domain.cms.response.CmsCode;
import com.chuhang.framework.domain.cms.response.CmsPageResult;
import com.chuhang.framework.domain.cms.response.CmsPostPageResult;
import com.chuhang.framework.exception.CustomException;
import com.chuhang.framework.exception.ExceptionCast;
import com.chuhang.framework.model.response.*;
import com.chuhang.manage_cms.config.RabbitmqConfig;
import com.chuhang.manage_cms.dao.CmsPageRepository;
import com.chuhang.manage_cms.dao.CmsSiteRepository;
import com.chuhang.manage_cms.dao.CmsTemplateRepository;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CmsPageService {
    /*
     *从IOC容器内获取CmsPageRepository对象
     */
    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    CmsSiteRepository cmsSiteRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    //分页条件查询cms信息业务逻辑代码
    public QueryResponseResult findList(int page,
                                        int size,
                                        QueryPageRequest queryPageRequest) {
        //1.进行参数检查
        if (page <= 0) {
            page = 1;
        }
        page = page - 1;
        if (size <= 0) {
            size = 5;
        }
        if (queryPageRequest == null) {
            queryPageRequest = new QueryPageRequest();
        }

        //=====处理模糊查询开始=====
        //2.创建ExampleMatcher
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();

        //2.1设置匹配器（模糊查询）,所有查询条件如果没有设置默认精确查，现在设置只有别名是模糊查
        exampleMatcher = exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());

        //3.构建查询条件
        CmsPage cmsPage = new CmsPage();
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        if (StringUtils.isNotEmpty(queryPageRequest.getPageId())) {
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }


        //4.创建Example实例
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);

        //=====处理模糊查询结束=====
        //5.分页对象
        Pageable pageRequest = PageRequest.of(page, size);

        //6.分页查询
        Page<CmsPage> cmsPages = cmsPageRepository.findAll(example, pageRequest);

        //7.响应结果对象
        QueryResult<CmsPage> queryResult = new QueryResult<CmsPage>();

        //8.数据+总条数
        List<CmsPage> pageList = cmsPages.getContent();
        long total = cmsPages.getTotalElements();

        //9.返回结果，设置返回的状态码和查询的对象
        QueryResult<CmsPage> result = new QueryResult<>();
        result.setList(pageList);
        result.setTotal(total);
        return new QueryResponseResult(CommonCode.SUCCESS, result);
    }

    //添加页面业务逻辑代码
    public CmsPageResult add(CmsPage cmsPage) {

        //不可预知异常，
        //if (cmsPage==null){
        ////抛异常，参数非法
        //}

        //校验页面是否存在，根据页面名称、站点Id、页面webpath查询
        CmsPage cmsPageInMongoDB = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());

        //定义全局异常的切入点
        if(cmsPageInMongoDB!=null){
            //抛出异常，页面已存在
            //throw new CustomException(CommonCode.CMS_ADDPAGE_EXISTSNAME);
            //调用自定义抛出异常类
            ExceptionCast.cast(CommonCode.CMS_ADDPAGE_EXISTSNAME);
        }
        //没有相同数据，可以添加
        //添加的主键由spring Data 自动帮你生成
        cmsPage.setPageId(null);
        //调用添加的方法
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
    }

    //根据页面id查询信息回显
    public CmsPage getById(String id) {
        //jdk1.8里新增的工具类【Optional】，里面有判断是否为空的方法
        //根据id查询对象
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        //判读是否为空
        if (optional.isPresent()) {
            //不为空，则根据optional.get()，获取CmsPage对象
            CmsPage cmsPage = optional.get();
            return cmsPage;
        }
        //为空，则没有该对象
        return null;
    }

    //修改页面业务逻辑代码
    public CmsPageResult edit(String id, CmsPage cmsPage) {
        /*
         * mongodb没有update方法，只能通过save()进行修改
         */
        //jdk1.8里新增的工具类【Optional】，里面有判断是否为空的方法
        //根据id查询对象
        CmsPage cmsInDb = this.getById(id);
        //判读是否为空
        if (cmsInDb!=null) {
            //不为空，则根据optional.get()，获取CmsPage对象,并重新赋值
            //更新模板id
            cmsInDb.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            cmsInDb.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            cmsInDb.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            cmsInDb.setPageName(cmsPage.getPageName());
            //更新访问路径
            cmsInDb.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            cmsInDb.setPagePhysicalPath(cmsPage.getPagePhysicalPath());

            //更新dataUrl
            cmsInDb.setDataUrl(cmsPage.getDataUrl());

            //调用mongodb的save()方法
            CmsPage save = cmsPageRepository.save(cmsInDb);
            if (save!=null) {
                return new CmsPageResult(CommonCode.SUCCESS, save);
            }
        }
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    //删除页面
    public ResponseResult delete(String id){
        //jdk1.8里新增的工具类【Optional】，里面有判断是否为空的方法
        //根据id查询对象
        CmsPage cmsPage = this.getById(id);
        if (cmsPage!=null) {
            //删除页面
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

//-----------------------------页面静态化技术--------------------------//
    /**
     *页面静态化的功能：
     * 1.获取数据【getModelByPageId(String pageId)】
     *      1.1根据 pageId 查询对应的 cms_page 信息，并拿到其中的 DataUrl
     *      1.2使用 RestTemplate 远程请求 URL
     * 2.获取页面模板【getTemplateByPageId(String pageId)】
     *      2.1根据pageId查询对应的 cms_page 信息，从中获取 templateId
     *      2.2在根据 templateId 去获取cms_template集合中的 templateFileId
     *      2.3然后根据 templateFileId
     * 3.执行页面静态化【generateHtml(Map,Model)】
     */
    public String getPageHtml(String pageId){
        //1，获取页面模型数据
        Map model=this.getModelByPageId(pageId);
        if (model==null){
            //获取页面模型数据为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }

        //2，获取页面模板
        String templateContent=this.getTemplateByPageId(pageId);
        if (StringUtils.isEmpty(templateContent)){
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }

        //3，执行静态化
        String html=this.generateHtml(templateContent,model);
        if (StringUtils.isEmpty(html)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return html;
    }

    /*
    * 1，获取页面模型数据
    */
    public Map getModelByPageId(String pageId){
        //查询页面信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出dataURL
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)){
            //从页面信息中找不到获取数据的url
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //使用 RestTemplate 远程请求 URL
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map model = forEntity.getBody();
        return model;
    }

    /*
     *2，获取页面模板
     * 入口：需要内容，不需要实体
     */
    public String getTemplateByPageId(String pageId){
        //查询页面信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //页面模板
        // 取出templateId
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)) {
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            //找到模板文件对象
            //获取模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //根据id查询文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            // 打开下载流对象
            GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建gridFsResource，用于获取流对象
            GridFsResource gridFsResource=new GridFsResource(gridFSFile,downloadStream);
            // 获取流中的数据
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 执行页面静态化
     */
    public String generateHtml(String templateContent, Map model){
        try {
            //生成配置类
            Configuration configuration=new Configuration(Configuration.getVersion());
            //模板加载器
            StringTemplateLoader stringTemplateLoader=new StringTemplateLoader();
            //相当于把你的模板（内容，不是实体）取个名字
            stringTemplateLoader.putTemplate("template",templateContent);
            //配置模板加载器
            configuration.setTemplateLoader(stringTemplateLoader);
            //获取模板
            Template template = configuration.getTemplate("template");
            //生成出口（内容，不是实体）
            String html= FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return html;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

//----------------------------页面发布--------------------------------//

    /**
     *页面发布：
     *  1.执行页面静态化
     *  2.将页面静态化文档存储到GridFS中
     *  3.向RabbitMQ消息
     */

    /**
     * 1.执行页面静态化
     * 2.将页面静态化文件存储到GridFS中
     * 3.发布页面 发送消息
     * @param pageId
     * @return
     */
    public ResponseResult postPage(String pageId){
        //1.执行页面静态化
        String pageHtml = getPageHtml(pageId);
        //2.将页面静态化文件存储到GridFs中
        CmsPage cmsPage = savePageHtmlToGridFs(pageId,pageHtml);
        //3.发布页面发送消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //保存页面静态化文件
    private CmsPage savePageHtmlToGridFs(String pageId, String pageHtml) {
        //先拿到页面信息
        CmsPage cmsPage = getById(pageId);
        if (cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }

        InputStream inputStream = null;
        ObjectId htmlFileId = null;
        try {
            //将pageHtml的内容转换为流
            inputStream = IOUtils.toInputStream(pageHtml, "utf-8");
            //将html文件内容保存到GridFs中
            htmlFileId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
            System.out.println("发送的消息::"+htmlFileId);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //将html文件id更新到cmsPage中
        cmsPage.setHtmlFileId(htmlFileId.toString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }

    //发布页面发送消息
    private void sendPostPage(String pageId) {
        //先拿到页面信息
        CmsPage cmsPage = getById(pageId);
        if (cmsPage==null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        Map<String,String> msgmap = new HashMap<>();
        msgmap.put("pageId",pageId);

        //将内容转换为json串
        String msg = JSON.toJSONString(msgmap);

        System.out.println("pageId:"+msg);
        //将获取的站点id作为routingKey
        String siteId = cmsPage.getSiteId();
        //发布消息到mq
        this.rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId, msg);
    }

//================================================================================//
    /**
     *添加页面：比如一些需要页面预览时没有pageId,所以给他们添加一个新的页面生产新的id
     * 列如：课程预览页面
     */
    public CmsPageResult saveOrUpdate(CmsPage cmsPage){
        //校验页面是否存在，根据页面名称、站点Id、页面webpath查询
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(),
                cmsPage.getSiteId(),
                cmsPage.getPageWebPath());
        if (cmsPage1==null){
            //没有，则添加
            return this.add(cmsPage);
        }else {
            //否则，更新
            return this.edit(cmsPage1.getPageId(),cmsPage);
        }

    }

    /**
     *一键发布页面
     */
    public CmsPostPageResult postPageQuick(CmsPage cmsPage){
        //1.添加页面
        CmsPageResult cmsPageResult = this.saveOrUpdate(cmsPage);
        if (!cmsPageResult.isSuccess()){
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }
        CmsPage cmsPage1=cmsPageResult.getCmsPage();
        //2.要发布的id
        String pageId = cmsPage1.getPageId();
        //3.发布页面
        ResponseResult responseResult = this.postPage(pageId);
        if (!responseResult.isSuccess()){
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }
        //4.得到页面的url
        //页面url=站点域名+站点webpath+页面webpath+页面名称
        //站点id
        String siteId = cmsPage1.getSiteId();
        //拿站点id查询站点信息
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        //站点域名
        String siteDomain = cmsSite.getSiteDomain();
        //站点web路径
        String siteWebPath = cmsSite.getSiteWebPath();
        //页面web路径
        String pageWebPath = cmsPage1.getPageWebPath();
        //页面名称
        String pageName = cmsPage1.getPageName();

        //5.拼接pageUrl  页面的web访问地址
        String pageUrl=siteDomain+siteWebPath+pageWebPath+pageName;

        return new CmsPostPageResult(CommonCode.SUCCESS,pageUrl);
    }

    /**
    * 根据站点id查询站点信息
    * */
    private CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }
}
