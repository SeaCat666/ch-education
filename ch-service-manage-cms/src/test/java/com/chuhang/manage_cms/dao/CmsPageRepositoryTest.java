package com.chuhang.manage_cms.dao;

import com.chuhang.framework.domain.cms.CmsPage;
import com.chuhang.framework.domain.cms.CmsPageParam;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/*
*Mongodb测试类
*/
@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {
    /*
    *从IOC容器内获取CmsPageRepository对象
    */
    @Autowired
    CmsPageRepository cmsPageRepository;


    /**
     * 查询单个
     */
    @Test
    public void testFindOne() {
        Optional<CmsPage> byId = cmsPageRepository.findById("5a795ac7dd573c04508f3a56");
        System.out.println("结果=========="+byId.get());
    }

    /*
     *查询所有
     */
    @Test
    public void testFindAll() {
        List<CmsPage> cmsPageList = cmsPageRepository.findAll();
        System.out.println("结果==========" + cmsPageList);
    }

    /*
     *分页查询
     */
    @Test
    public void testFindPage() {
        //1.设置分页参数
        int page = 0;
        int size = 10;
        //2.PageRequest：Mongodb分页请求API
        Pageable pageable = PageRequest.of(page, size);
        //3.调用分页查询传入pageRequest获得Page对象，Page对象封装了分页的所有相关数据
        Page<CmsPage> cmsPages = cmsPageRepository.findAll(pageable);
        //4.获取分页数据
        List<CmsPage> pageList = cmsPages.getContent();
        //分页总条数
        long total = cmsPages.getTotalElements();
        //5.输出结果
        System.out.println("内容：" + pageList + "总条数：" + total);
    }

    /*
    *自定义条件查询
    */
    @Test
    public void testFindByPageName(){
        CmsPage cmsPage = cmsPageRepository.findByPageName("index.html");
        System.out.println("内容："+cmsPage);
    }

    /*
    *分页+条件查询
    */
    @Test
    public void testFindByPageAndExample(){
        //1.设置分页参数
        int page = 0;
        int size = 10;

        //2.PageRequest：Mongodb分页请求API
        Pageable pageable = PageRequest.of(page, size);

        //3.初始化数据，就是设置你的查询条件
        CmsPage cmsPage=new CmsPage();
        cmsPage.setPageAliase("轮播");

        //4.初始化ExamplMatcher【示例匹配器】
        ExampleMatcher matcher=ExampleMatcher.matching();
        matcher=matcher.withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());

        //5.了解Exampl:
            /*
            * example包含两个部分组成，分别是probe、matcher两部分，
            * 其中probe是实体类，包含用于查询的参数，
            * 另一个matcher是比对规则，用于设置比对语句
            * */
        Example<CmsPage> example = Example.of(cmsPage,matcher);

        //6.调用查询所有
        Page<CmsPage> cmsPages = cmsPageRepository.findAll(example, pageable);
        List<CmsPage> pageList = cmsPages.getContent();
    }

    /*
     *增
     */
    @Test
    public void testAdd() {
        //1.初始化CmsPage，并且给每个属性赋值
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId("s01");
        cmsPage.setTemplateId("t01");
        cmsPage.setPageName("测试页面");
        cmsPage.setPageCreateTime(new Date());

        //2.初始化CmsPageParam,并且给CmsPageParam里面的属性赋值
        //因为CmsPage里面的pageParams【参数列表】属性需要，给作为新增的参数
        //创建List<CmsPageParam> 用来装参数列表
        List<CmsPageParam> list = new ArrayList<>();
        CmsPageParam cmsPageParam = new CmsPageParam();
        cmsPageParam.setPageParamName("param1");
        cmsPageParam.setPageParamValue("value1");
        list.add(cmsPageParam);
        //给参数列表赋值
        cmsPage.setPageParams(list);
        cmsPageRepository.save(cmsPage);

        //3.测试输出
        System.out.println(cmsPage);
    }

    /*
     *改
     */
    @Test
    public void testUpdate() {
        //1.jdk1.8里新增的工具类【Optional】，里面有判断是否为空的方法
        //根据id查询对象
        Optional<CmsPage> optional = cmsPageRepository.findById("62340cdae2af4a0b2c7fa177");

        //2.判断是否为空
        if (optional.isPresent()) {
            //不为空，则根据optional.get()，获取CmsPage对象
            CmsPage cmsPage = optional.get();
            cmsPage.setPageName("测试页面9999");

            //测试
            cmsPageRepository.save(cmsPage);
        }
    }

    /*
    *删
    */
    @Test
    public void testDelete(){
      //根据id删除
        cmsPageRepository.deleteById("62340edde2af4a24d43805e2");
    }
}
