package com.chuhang.manage_cms.config;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/*
*GridFSBucket类用于打开下载流对象
*/
public class MongoConfig {
    // yml中记得设置database: chzx_cms
    @Value("${spring.data.mongodb.database}")
    String mongodb;

    @Bean
    public GridFSBucket getGridFSBucket(MongoClient mongoClient){
        //获取数据库
        MongoDatabase database = mongoClient.getDatabase(mongodb);
        GridFSBucket bucket = GridFSBuckets.create(database);
        return bucket;
    }
}
