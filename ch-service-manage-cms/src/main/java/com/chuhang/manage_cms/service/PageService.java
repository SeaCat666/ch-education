package com.chuhang.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.chuhang.framework.domain.cms.CmsPage;
import com.chuhang.framework.domain.cms.CmsSite;
import com.chuhang.framework.domain.cms.CmsTemplate;
import com.chuhang.framework.domain.cms.request.QueryPageRequest;
import com.chuhang.framework.domain.cms.response.CmsCode;
import com.chuhang.framework.domain.cms.response.CmsPageResult;
import com.chuhang.framework.domain.cms.response.CmsPostPageResult;
import com.chuhang.framework.exception.ExceptionCast;
import com.chuhang.framework.model.response.CommonCode;
import com.chuhang.framework.model.response.QueryResponseResult;
import com.chuhang.framework.model.response.QueryResult;
import com.chuhang.framework.model.response.ResponseResult;
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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class PageService {
    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CmsSiteRepository cmsSiteRepository;

    //一键发布页面
    public CmsPostPageResult postPageQuick(CmsPage cmsPage){
        //添加页面
        CmsPageResult save = this.saveOrUpdate(cmsPage);
        if(!save.isSuccess()){
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }
        CmsPage cmsPage1 = save.getCmsPage();
        //要布的页面id
        String pageId = cmsPage1.getPageId();
        //发布页面
        ResponseResult responseResult = this.postPage(pageId);
        if(!responseResult.isSuccess()){
            return new CmsPostPageResult(CommonCode.FAIL,null);
        }
        //得到页面的url
        //页面url=站点域名+站点webpath+页面webpath+页面名称
        //站点id
        String siteId = cmsPage1.getSiteId();
        //拿站点id查询站点信息
        CmsSite cmsSite = findCmsSiteById(siteId);
        //站点域名
        String siteDomain = cmsSite.getSiteDomain();
        //站点web路径
        String siteWebPath = cmsSite.getSiteWebPath();
        //页面web路径
        String pageWebPath = cmsPage1.getPageWebPath();
        //页面名称
        String pageName = cmsPage1.getPageName();
        //页面的web访问地址
        String pageUrl = siteDomain+siteWebPath+pageWebPath+pageName;
        return new CmsPostPageResult(CommonCode.SUCCESS,pageUrl);
    }

    //根据id查询站点信息
    public CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    //添加页面，如果已存在则更新页面
    public CmsPageResult saveOrUpdate(CmsPage cmsPage){
        //校验页面是否存在，根据页面名称、站点Id、页面webpath查询
        CmsPage cmsPage1 =
                cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(),
                        cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(cmsPage1 !=null){
            //更新
            return this.update(cmsPage1.getPageId(),cmsPage);
        }else{
            //添加
            return this.add(cmsPage);
        }
    }


    //添加页面，如果已存在则更新页面
    public CmsPageResult save(CmsPage cmsPage){
        //校验页面是否存在，根据页面名称、站点Id、页面webpath查询
        CmsPage cp =
                cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(),
                        cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(cp !=null){
            //更新
            return this.update(cp.getPageId(),cmsPage);
        }else{
            //添加
            return this.add(cmsPage);
        }
    }


    /**
     * 页面发布
     * 1.执行页面静态化
     * 2.将页面静态化文件存储到GraidFS中
     * 3.向MQ发消息
     */
    public ResponseResult postPage(String pageId){
        //1.执行静态化
        String pageHtml = this.getPageHtml(pageId);
        //2.保存静态化文件
        CmsPage cmsPage = saveHtml(pageId, pageHtml);
        //3.发送消息
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    /**
     * 保存静态页面内容html到GridFS
     */
    private CmsPage saveHtml(String pageId,String htmlContent){
        //先得到页面信息
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        ObjectId objectId = null;
        try {
            //将htmlContent内容转成输入流
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            //将html文件内容保存到GridFS
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将html文件id更新到cmsPage中
        cmsPage.setHtmlFileId(objectId.toString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }
    /**
     * 发送页面发布消息
     */
    private void sendPostPage(String pageId){
        //得到页面信息
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Map<String,String> msgMap = new HashMap<>();
        msgMap.put("pageId",pageId);
        //消息内容转成json串
        String msg = JSON.toJSONString(msgMap);
        //获取站点id作为routingKey
        String siteId = cmsPage.getSiteId();
        //发布消息到mq
        this.rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId, msg);
    }

    public String getPageHtml(String pageId) {
        //1、获取页面模型数据
        Map model = this.getModelByPageId(pageId);
        if (model == null) {
            //获取页面模型数据为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //2、获取页面模板
        String templateContent = getTemplateByPageId(pageId);
        if (StringUtils.isEmpty(templateContent)) {
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //3、执行静态化
        String html = generateHtml(templateContent, model);
        if (StringUtils.isEmpty(html)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return html;
    }

    /**
     * 获取页面模型数据
     */
    public Map getModelByPageId(String pageId){
        //查询页面信息
        CmsPage cmsPage = this.getById(pageId);
        if(cmsPage == null){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出dataUrl
        String dataUrl = cmsPage.getDataUrl();
        if(StringUtils.isEmpty(dataUrl)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }

    /**
     * 获取页面模板
     * 入口：需要内容，不需要实体
     */
    public String getTemplateByPageId(String pageId) {
        //查询页面信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null) {
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //页面模板
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)) {
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()) {
            CmsTemplate cmsTemplate = optional.get();
            //模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //找到模板文件对象
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建GridFsResource，获取GridFs资源（模板文件），参数：模板文件对象，下载流对象
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
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
    public String generateHtml(String template, Map model) {
        try {
            //生成配置类
            Configuration configuration = new Configuration(Configuration.getVersion());
            //模板加载器
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            //相当于把你的模板（内容，不是实体）取个名字
            stringTemplateLoader.putTemplate("template", template);
            //配置模板加载器
            configuration.setTemplateLoader(stringTemplateLoader);
            //获取模板
            Template template1 = configuration.getTemplate("template");
            //生成出口（内容，不是实体）
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template1, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 页面列表分页查询
     * @param page
     * @param size
     * @param queryPageRequest
     * @return
     */
    public QueryResponseResult<CmsPage> findList(int page, int size, QueryPageRequest queryPageRequest) {
        //0.参数的检查
        if (queryPageRequest==null){
            queryPageRequest = new QueryPageRequest();
        }
        if (page <= 0){
            page = 1;
        }
        page = page-1;
        if (size <=0){
            size = 10;
        }

        //4.初始化数据
        //4.1 初始化条件查询所需要的查询条件
        CmsPage cmsPage = new CmsPage();
        //现在这个对象是空的，所以没有条件
        //cmsPage.setPageName("hhh");
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }


        //4.2 初始化ExampleMatcher
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());

        //3.cmsPage是条件
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);

        //2.构建分页请求对象
        PageRequest pageRequest = PageRequest.of(page, size);

        //1.调用查询所有,这里拿到数据
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageRequest);

        //5.从处理好的分页数据中取目标数据
        List<CmsPage> content = all.getContent();
        long total = all.getTotalElements();
        //6.封装好分页数据
        QueryResult<CmsPage> queryResult = new QueryResult();
        queryResult.setList(content);
        queryResult.setTotal(total);

        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }

    //添加页面
    public CmsPageResult add(CmsPage cmsPage){
        //校验页面是否存在，根据页面名称、站点Id、页面webpath查询
        CmsPage cmsInDb = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());

        if(cmsInDb==null){
            //确保添加页面主键由spring data 自动生成
            cmsPage.setPageId(null);
            cmsPageRepository.save(cmsPage);
            //返回结果
            CmsPageResult cmsPageResult = new CmsPageResult(CommonCode.SUCCESS,cmsPage);
            return cmsPageResult;
        }else{//校验页面是否存在，已存在则抛出异常
            // 抛出异常，已存在相同的页面名称
            //throw new CustomException(CmsCode.CMS_ADDPAGE_EXISTSNAME);
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        // 存在页面，返回失败
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    //根据id查询
    public CmsPage getById(String id){
        //Optional是jdk1.8对获取的对象是否为空的一个标准化
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        System.out.println("现在调用了spring data的方法---id是"+id);
        if(optional.isPresent()){
            //true为有值
            CmsPage cmsPage = optional.get();
            System.out.println("值来喽---id是"+id);
            return cmsPage;
        }
        System.out.println("没有值---id是"+id);
        return null;
    }



    public CmsPageResult update(String id,CmsPage cmsPage){
        CmsPage byId = this.getById(id);
        if(byId!=null){
            //更新模板id
            byId.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            byId.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            byId.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            byId.setPageName(cmsPage.getPageName());
            //更新访问路径
            byId.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            byId.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新dataUrl
            byId.setDataUrl(cmsPage.getDataUrl());

            //save方法自带的，保存数据之后返回最新的数据
            CmsPage save = cmsPageRepository.save(byId);
            if(save!=null){
                return new CmsPageResult(CommonCode.SUCCESS,save);
            }
            return null;
        }
        //返回失败
        return new CmsPageResult(CommonCode.FAIL,null);
    }


    public void del(String id) {
        cmsPageRepository.deleteById(id);
    }
}
