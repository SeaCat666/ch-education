package com.chuhang.search.service;

import com.chuhang.framework.domain.course.CoursePub;
import com.chuhang.framework.domain.search.CourseSearchParam;
import com.chuhang.framework.model.response.CommonCode;
import com.chuhang.framework.model.response.QueryResponseResult;
import com.chuhang.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EsCourseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsCourseService.class);
    @Value("${chuhang.elasticsearch.course.index}")
    private String es_index;
    @Value("${chuhang.elasticsearch.course.type}")
    private String es_type;
    @Value("${chuhang.elasticsearch.course.source_field}")
    private String source_field;
    @Autowired
    RestHighLevelClient restHighLevelClient;

    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
        //【0】.判断搜索条件
        if(courseSearchParam == null){
            courseSearchParam = new CourseSearchParam();
        }
        //【1】.创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(es_index);
        //1.1设置搜索类型
        searchRequest.types(es_type);
        //1.2搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //【2】.源字段过虑
        String[] source_fields = source_field.split(",");
        searchSourceBuilder.fetchSource(source_fields, new String[]{});

        //5.1搜索方式: bool搜索(后面有多个字段的关键字搜索还有分类搜索，所以用bool)
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //【5】根据关键字搜索
        if(StringUtils.isNotEmpty(courseSearchParam.getKeyword())){
            //匹配关键字
            MultiMatchQueryBuilder multiMatchQueryBuilder =
                    QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name","teachplan","description")
                            //设置匹配占比
                            .minimumShouldMatch("70%")
                            //提升另个字段的Boost值
                            .field("name",10);

            boolQueryBuilder.must(multiMatchQueryBuilder);
        }

        //【8】过滤
        if(StringUtils.isNotEmpty(courseSearchParam.getMt())){
            //根据一级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        if(StringUtils.isNotEmpty(courseSearchParam.getSt())){
            //根据二级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        if(StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            //根据难度等级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }

        //5.2 设置bool查询到searchSourceBuilder
        searchSourceBuilder.query(boolQueryBuilder);

        //【9】分页
        if(page<=0){
            page = 1;
        }
        if(size<=0){
            size = 12;
        }
        //起始记录下标
        int from = (page-1) * size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        //【10】设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //10.1 设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);

        //【3】.向搜索请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;
        //分页结果
        QueryResult<CoursePub> queryResult = new QueryResult<>();
        //数据列表
        List<CoursePub> list = new ArrayList<>();
        try {
            //【4】.执行搜索，向ES发起htpp请求
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //【6】获取响应的结果
            SearchHits hits = searchResponse.getHits();
            //6.1 获取总记录数
            long totalHits = hits.getTotalHits().value;
            //【设置分页的总条数】
            queryResult.setTotal(totalHits);
            //6.2 获取匹配度高的记录
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                CoursePub coursePub = new CoursePub();
                //获取源文档内容
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //取出名称
                String name = (String) sourceAsMap.get("name");

                //10.2 取出高亮字段
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if(highlightFields.get("name")!=null){
                    HighlightField highlightField = highlightFields.get("name");
                    Text[] fragments = highlightField.fragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for(Text text:fragments){
                        stringBuffer.append(text);
                    }
                    name = stringBuffer.toString();
                }
                coursePub.setName(name);
                //图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //价格
                Double price = null;
                if(sourceAsMap.get("price")!=null ){
                    price = (Double) sourceAsMap.get("price");
                }
                coursePub.setPrice(price);
                //老价格
                Double price_old = null;
                if(sourceAsMap.get("price_old")!=null ){
                    price_old = (Double) sourceAsMap.get("price_old");
                }
                coursePub.setPrice_old(price_old);
                //将coursePub放入到要返回的List集合
                list.add(coursePub);
            }

        } catch (IOException e) {
            return new QueryResponseResult(CommonCode.SUCCESS,new QueryResult<CoursePub>());
        }
        //【设置搜索到的所有数据】
        queryResult.setList(list);
        //【7】返回
        QueryResponseResult<CoursePub> coursePubQueryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return coursePubQueryResponseResult;
    }
}