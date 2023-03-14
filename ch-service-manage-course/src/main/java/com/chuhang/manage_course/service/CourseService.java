package com.chuhang.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.chuhang.framework.domain.cms.CmsPage;
import com.chuhang.framework.domain.cms.response.CmsPageResult;
import com.chuhang.framework.domain.cms.response.CmsPostPageResult;
import com.chuhang.framework.domain.course.*;
import com.chuhang.framework.domain.course.ext.CourseInfo;
import com.chuhang.framework.domain.course.ext.CourseView;
import com.chuhang.framework.domain.course.ext.TeachplanNode;
import com.chuhang.framework.domain.course.request.CourseListRequest;
import com.chuhang.framework.domain.course.response.AddCourseResult;
import com.chuhang.framework.domain.course.response.CourseCode;
import com.chuhang.framework.domain.course.response.CoursePublishResult;
import com.chuhang.framework.exception.ExceptionCast;
import com.chuhang.framework.model.response.CommonCode;
import com.chuhang.framework.model.response.QueryResponseResult;
import com.chuhang.framework.model.response.QueryResult;
import com.chuhang.framework.model.response.ResponseResult;
import com.chuhang.manage_course.client.CmsPageClient;
import com.chuhang.manage_course.dao.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TeachplanRepository teachplanRepository;

    @Autowired
    private CourseBaseRepository courseBaseRepository;

    @Autowired
    private CourseMarketRepository courseMarketRepository;

    @Autowired
    private CoursePicRepository coursePicRepository;

    //查询课程计划
    public TeachplanNode findTeachplanList(String courseId){
        TeachplanNode teachplanNode = courseMapper.selectList(courseId);
        return teachplanNode;
    }

    /**
     *添加课程计划
     */
    public ResponseResult addTeachplan(Teachplan teachplan){
        //1.判断非法参数
        if (teachplan==null
                || StringUtils.isEmpty(teachplan.getCourseid())
                || StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        //2.取出用户填写表单中的节点（可能填了，也可能没填），
        // 填了代表用此parentid去挂子节点，如果没有填，就需要去找出该节点
        String parentid = teachplan.getParentid();

        //3.判断用户是否选择了节点
        if (StringUtils.isEmpty(parentid)){
            //没填，代表用户没有选择上一节节点，那么，添加的可能是1级或者2级，所以需要找到它的上一级节点的parentid
            parentid=getTeachplanRoot(teachplan.getCourseid());
        }
        //填了，代表用户有选择上一节节点，那么，添加的可能是2级或者3级，
        //4.面向对象原则，将用户的信息进行封装，尽量不操作用户传递的对象
        Teachplan teachplan2=new Teachplan();
        BeanUtils.copyProperties(teachplan,teachplan2);

        //5.补充该节点的其他信息，需要该节点挂载到某个父节点下
        teachplan2.setParentid(parentid);

        //继续添加grade,是2?还是3？
        //所以得先根据查询出来得上一级节点，去查询该节点的信息
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan parentNode=null;
        if (optional.isPresent()){
            parentNode = optional.get();
        }
        //判断等级
        if (parentNode.getGrade().equals("1")){
            teachplan2.setGrade("2");
        }else {
            teachplan2.setGrade("3");
        }

        //保存数据
        teachplanRepository.save(teachplan2);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 查询上一级节点
     */
    private String getTeachplanRoot(String courseId){
        //1.查询是否存在一级节点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if (teachplanList==null || teachplanList.size()<0) {
            //不存在，则添加为一级
            Teachplan teachplan1 = new Teachplan();
            teachplan1.setParentid("0");
            teachplan1.setGrade("1");
            teachplan1.setPname(getCourseNameByCourseId(courseId));//查询课程名称【course_base表】
            teachplan1.setCourseid(courseId);
            teachplan1.setStatus("0");//默认为未发布状态

            //将准备好的一级节点(课程节点)保存到teachplan表
            Teachplan teachplan = teachplanRepository.save(teachplan1);
            return teachplan.getId();
        }
        //存在,直接返回
        return teachplanList.get(0).getId();
    }

    /**
     * 根据课程id获取课程名称
     * @param courseId
     * @return
     */
    private String getCourseNameByCourseId(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_NOT_EXIST);
        }
        return optional.get().getName();
    }

    /**
    * 分页查询课程信息
    */
    public QueryResponseResult<CourseInfo> findCourseList(int page,
                                                          int size,
                                                          CourseListRequest courseListRequest){
        //1.非空判断
        if (courseListRequest==null){
            courseListRequest=new CourseListRequest();
        }
        if (page<=0){
            page=0;
        }
        if (size<=0){
            size=8;
        }

        //2.设置分页参数
        PageHelper.startPage(page,size);

        //3.分页查询
        List<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        //封装到PageInfo
        PageInfo<CourseInfo> pageInfo=new PageInfo<>(courseListPage);

        //4.查询结果集
        QueryResult<CourseInfo> courseInfoQueryResult=new QueryResult<>();
        courseInfoQueryResult.setList(pageInfo.getList());//设置数据列表
        courseInfoQueryResult.setTotal(pageInfo.getTotal());//设置总数

        //5.返回查询响应结果
        return new QueryResponseResult<CourseInfo>(CommonCode.SUCCESS,courseInfoQueryResult);
    }

    /**
     * 添加课程提交
     */
    public AddCourseResult addCourseBase(CourseBase courseBase){
        //课程状态默认为未发布
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    /**
    * 1.根据课程id查询课程基本信息进行回显
     * 2.做课程预览页面要通过courseId查询课程名称
    */
    public CourseBase findCourseBaseById(String courseid){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseid);
        if (optional.isPresent()){
            CourseBase courseBase = optional.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        return null;
    }

    /**
    * 根据课程id修改课程基本信息
    */
    public ResponseResult updateCourseBaseById(String courseId,CourseBase courseBase){
        CourseBase courseBaseById = findCourseBaseById(courseId);
        if (courseBaseById==null){
            ExceptionCast.cast(CourseCode.COURSE_NOT_EXIST);
        }
        if (courseBase==null){
            courseBase=new CourseBase();
        }

        CourseBase courseBase1=new CourseBase();
        BeanUtils.copyProperties(courseBaseById,courseBase1);

        courseBase1.setId(courseBase.getId());
        courseBase1.setName(courseBase.getName());
        courseBase1.setUsers(courseBase.getUsers());
        courseBase1.setGrade(courseBase.getGrade());
        courseBase1.setStudymodel(courseBase.getStudymodel());
        courseBase1.setMt(courseBase.getMt());
        courseBase1.setSt(courseBase.getSt());
        courseBase1.setDescription(courseBase.getDescription());

        courseBaseRepository.save(courseBase1);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     *根据id获取课程营销信息
     */
    public CourseMarket getCourseMarketById(String courseId){
        if (StringUtils.isEmpty(courseId)){
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_NOT_EXIST);
        }
        CourseMarket courseMarket = optional.get();
        return courseMarket;
    }

    /**
     *根据id修改课程营销信息
     */
    public ResponseResult updateCourseMarketById(String courseId,CourseMarket courseMarket){
        //1.根据课程id获取课程营销信息
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);

        //2.判断课程营销信息存在
        if (!optional.isPresent()){
           //不存在，表单提交为添加
            courseMarketRepository.save(courseMarket);
            return new ResponseResult(CommonCode.SUCCESS);
        }else {
           //存在，表单提交为修改
            CourseMarket courseMarket1=new CourseMarket();
            BeanUtils.copyProperties(optional.get(),courseMarket1);

            courseMarket1.setId(courseMarket.getId());
            courseMarket1.setCharge(courseMarket.getCharge());
            courseMarket1.setValid(courseMarket.getValid());
            courseMarket1.setPrice(courseMarket.getPrice());
            courseMarket1.setStartTime(courseMarket.getStartTime());
            courseMarket1.setEndTime(courseMarket.getEndTime());
            courseMarket1.setQq(courseMarket.getQq());
            //修改
            courseMarketRepository.save(courseMarket1);
            return new ResponseResult(CommonCode.SUCCESS);
        }
    }

    /**
     * 添加课程图片，如果有就是修改
     */
    @Transactional
    public ResponseResult saveCoursePic(String courseId,String pic){
        //查询课程图片
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        CoursePic coursePic=null;
        if (optional.isPresent()){
            //存在，则修改
             coursePic = optional.get();
            //为了严谨一点，使用新创建的对象
             CoursePic coursePic1=new CoursePic();
             BeanUtils.copyProperties(coursePic,coursePic1);

             coursePic1.setCourseid(coursePic.getCourseid());
             coursePic1.setPic(coursePic.getPic());

             coursePicRepository.save(coursePic1);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        //没有课程图片则新建对象
        if(coursePic == null){
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     *课程图片回显
     */
    public CoursePic findCoursepic(String courseId){
        Optional<CoursePic>  optional= coursePicRepository.findById(courseId);
        if (optional.isPresent()){
            CoursePic coursePic = optional.get();
            return coursePic;
        }
        return null;
    }

    /**
     *根据id删除课程图片
     */
    @Transactional
    public ResponseResult deleteCoursePic(String courseId){
        //执行删除，返回1表示删除成功，返回0表示删除失败
        long result = coursePicRepository.deleteByCourseid(courseId);
        if (result>0){
            //删除成功
            return new ResponseResult(CommonCode.SUCCESS);
        }
        //删除失败
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
    * 课程详情视图数据查询
    */
    public CourseView getCoruseView(String id){
        CourseView courseView=new CourseView();

        //查询课程基本信息
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(id);
        if (optionalCourseBase.isPresent()){
            CourseBase courseBase = optionalCourseBase.get();
            courseView.setCourseBase(courseBase);
        }
        //查询课程营销信息
        Optional<CourseMarket> optionalCourseMarket = courseMarketRepository.findById(id);
        if (optionalCourseMarket.isPresent()){
            CourseMarket courseMarket = optionalCourseMarket.get();
            courseView.setCourseMarket(courseMarket);
        }
        //查询课程图片信息
        Optional<CoursePic> optionalCoursePic = coursePicRepository.findById(id);
        if (optionalCoursePic.isPresent()){
            CoursePic coursePic = optionalCoursePic.get();
            courseView.setCoursePic(coursePic);
        }
        //查询课程计划信息
        TeachplanNode teachplanNode=courseMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);

        //返回课程详情包含的数据
        return courseView;
    }

//=====================课程预览+课程发布==========================================//
    /**
    * 这些是活数据
    */
    @Value("${course‐publish.dataUrlPre}")
    private String publish_dataUrlPre;

    @Value("${course‐publish.pagePhysicalPath}")
    private String publish_page_physicalpath;

    @Value("${course‐publish.pageWebPath}")
    private String publish_page_webpath;

    @Value("${course‐publish.siteId}")
    private String publish_siteId;

    @Value("${course‐publish.templateId}")
    private String publish_templateId;

    @Value("${course‐publish.previewUrl}")
    private String previewUrl;

    @Autowired
    private CmsPageClient cmsPageClient;

    @Autowired
    private CoursePubRepository coursePubRepository;

    /**
     *课程预览
     */
    public CoursePublishResult preview(String courseId){
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(courseId+".html");
        //页面别名
        cmsPage.setPageAliase(courseBase.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);

        System.out.println("aaaaaaaaaaaaaaa"+cmsPage);

        //远程请求cms保存页面信息
        CmsPageResult cmsPageResult = cmsPageClient.saveOrUpdate(cmsPage);
        if(!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //页面id
        String pageId = cmsPageResult.getCmsPage().getPageId();
        //页面url
        String pageUrl = previewUrl+pageId;
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    /**
     *课程发布
     */
    @Transactional
    public CoursePublishResult publish(String courseId){
        CmsPostPageResult cmsPostPageResult = this.publish_page(courseId);
        if(!cmsPostPageResult.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //更新课程状态
        CourseBase courseBase = this.saveCoursePubState(courseId);
        //课程索引...
        CoursePub coursePub = this.createCoursePub(courseId);
        //将coursePub对象保存到数据库
        this.saveCoursePub(courseId, coursePub);

        //课程缓存...
        //页面url
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);

    }

    //发布课程正式页面
    public CmsPostPageResult publish_page(String courseId){
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(courseId+".html");
        //页面别名
        cmsPage.setPageAliase(courseBase.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);

        //发布页面
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        return cmsPostPageResult;
    }

    //更新发布课程状态
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //更新发布课程状态为【已发布】
        courseBase.setStatus("202002");
        CourseBase save = courseBaseRepository.save(courseBase);
        return save;
    }

    /**
    * 创建coursePub对象
    */
    public CoursePub createCoursePub(String courseId){
        CoursePub coursePub=new CoursePub();
        coursePub.setId(courseId);

        //查询课程基本信息
        Optional<CourseBase> optionalCourseBase = courseBaseRepository.findById(courseId);
        if (optionalCourseBase.isPresent()){
            CourseBase courseBase = optionalCourseBase.get();
            //因为CoursePub和CourseBase有相同属性，所以可以直接调用BeanUtils的复制,将CourseBase添加到CoursePub
            BeanUtils.copyProperties(courseBase,coursePub);
        }

        //查询课程图片
        Optional<CoursePic> optionalCoursePic = coursePicRepository.findById(courseId);
        if (optionalCoursePic.isPresent()){
            CoursePic coursePic = optionalCoursePic.get();
            BeanUtils.copyProperties(coursePic,coursePub);
        }

        //查询课程营销
        Optional<CourseMarket> optionalCourseMarket = courseMarketRepository.findById(courseId);
        if (optionalCourseMarket.isPresent()){
            CourseMarket courseMarket = optionalCourseMarket.get();
            BeanUtils.copyProperties(courseMarket,coursePub);
        }

        //查询课程列表计划
        TeachplanNode teachplanNode = courseMapper.selectList(courseId);
        //将课程计划转成json
        String teachplanString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teachplanString);

        return coursePub;
    }

    /**
    * 将coursePub对象保存到MysqlDB中去
    * */
    public CoursePub saveCoursePub(String courseId,CoursePub coursePub){
        CoursePub coursePubInDB=null;
        Optional<CoursePub> pubOptional = coursePubRepository.findById(courseId);
        if (pubOptional.isPresent()){
            coursePubInDB=pubOptional.get();
        }else {
            coursePubInDB=new CoursePub();
        }
        //将coursePub对象中的信息保存到 coursePubInDB 中
        BeanUtils.copyProperties(coursePub,coursePubInDB);
        //设置主键
        coursePubInDB.setId(courseId);
        //更新时间戳为最新时间，给logstach使用
        coursePubInDB.setTimestamp(new Date());
        //设置发布时间
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy‐MM‐dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePubInDB.setPubTime(date);

        //保存
        coursePubRepository.save(coursePubInDB);
        return coursePubInDB;
    }
}
