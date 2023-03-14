package com.chuhang.manage_cms.http;

import com.mongodb.client.gridfs.*;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
/*
*这是一个 GridFs存文件测试类
*/
public class CridFsTest {
    //注入操作GridFs的对象
    @Autowired
    GridFsTemplate gridFsTemplate;


    @Autowired
    GridFSBucket gridFSBucket;

    //GridFs存文件测试
    @Test
    public void testStroe() throws FileNotFoundException {
        // 要存储的文件，记得拷贝到指定的目录下
        //File file=new File("D:/index_banner.ftl");
        File file=new File("E:/Develop/chuhang/course.ftl");
        //定义要输出的流
        FileInputStream fileInputStream=new FileInputStream(file);
        // 向GridFs存储文件，并获得id
        //ObjectId objectId = gridFsTemplate.store(fileInputStream, "index_banner01.ftl");
        ObjectId objectId = gridFsTemplate.store(fileInputStream, "course.ftl");
        //输出id
        //System.out.println(objectId);//轮播图的模板id：62446f18e189d02034b45700
        System.out.println(objectId);//课程详情页面的模板id：625e60406c99853750f208b5
    }

    //GridFs取文件测试
    @Test
    public void testBucket() throws IOException {
        // 获得的文件id(例如刚存的时候生成的)
        String fileId="62446f18e189d02034b45700";
        //根据id查询文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        // 打开下载流对象
        GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建gridFsResource，用于获取流对象
        GridFsResource gridFsResource=new GridFsResource(gridFSFile,downloadStream);
        // 获取流中的数据
        String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");

        //输出到控制台查看
        System.out.println("test111:\n"+content);

    }

    //GridFs删除文件测试
    @Test
    public void testDelFile(){
        //根据文件id删除fs.files和fs.chunks中的记录
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is("625cfd9b6c99854eb0609417")));
    }

}
