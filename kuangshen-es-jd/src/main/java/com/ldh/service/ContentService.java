package com.ldh.service;

import com.alibaba.fastjson.JSON;
import com.ldh.result.Content;
import com.ldh.utils.ElasticSearchConstant;
import com.ldh.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//业务逻辑
@Service("AdminContentService")
@Transactional
public class ContentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //解析数据存入ES 索引中
    public Boolean paresContent(String keyword) throws Exception {
        //查询数据
        List<Content> contents= new HtmlParseUtil().parseDJ(keyword);
        //把查询到的数据放入ES 中
        //创建BulkRequest请求
        BulkRequest bulkRequest=new BulkRequest();
        bulkRequest.timeout("2m");

        for (int i=0;i<contents.size();i++){
            bulkRequest.add(new IndexRequest(ElasticSearchConstant.JD_INDEX)
                    .source(JSON.toJSONString(contents.get(i)), XContentType.JSON));
        }

        //发送请求
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        //是否执行失败---返回false:执行成功
        System.out.println(bulkResponse.hasFailures());

        return !bulkResponse.hasFailures();
    }

    //删除ES数据
    public Boolean deleteContent(String keyword) throws Exception {
        Boolean success=true;
        //条件搜索
        SearchRequest searchRequest=new SearchRequest(ElasticSearchConstant.JD_INDEX);
        SearchSourceBuilder sourceBuilder=new SearchSourceBuilder();
        //分页
        sourceBuilder.from(1);
        sourceBuilder.size(100);
        //匹配查询
        MatchQueryBuilder title = QueryBuilders.matchQuery("title", keyword);
        //构建查询
        sourceBuilder.query(title);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse= restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //解析结果
        ArrayList<Map<String,Object>> list=new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            list.add(hit.getSourceAsMap());
        }

        if (searchResponse!=null && list.size()>0 ){
            //创建BulkRequest请求
            BulkRequest bulkRequest=new BulkRequest();
            bulkRequest.timeout("2m");
            /*  //删除
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                restHighLevelClient.delete(new DeleteRequest(ElasticSearchConstant.JD_INDEX, hit.getIndex()), RequestOptions.DEFAULT);
            }*/
            //删除
            for (SearchHit hit : searchResponse.getHits().getHits()){
                bulkRequest.add(new DeleteRequest(ElasticSearchConstant.JD_INDEX,hit.getId()));
            }
            //发送请求
            BulkResponse  bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            //是否执行失败---返回false:执行成功
            System.out.println(bulkResponse.hasFailures());
            success=bulkResponse.hasFailures();
        }
        System.out.println(!success);
        return !success;
    }

    //获取ES数据,实现搜索功能
    public List<Map<String,Object>> search(String keyword,Integer pageNO,Integer pageSize) throws Exception {
        if (pageNO<=1){pageNO=1;}

        //条件搜索
        SearchRequest searchRequest=new SearchRequest(ElasticSearchConstant.JD_INDEX);
        SearchSourceBuilder sourceBuilder=new SearchSourceBuilder();

        //分页
        sourceBuilder.from(pageNO);
        sourceBuilder.size(pageSize);

        //精准匹配
        TermQueryBuilder termQueryBuilder= QueryBuilders.termQuery("title",keyword);
        //匹配查询
        MatchQueryBuilder title = QueryBuilders.matchQuery("title", keyword);
        //构建查询
        sourceBuilder.query(title);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse= restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //解析结果
        ArrayList<Map<String,Object>> list=new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            list.add(hit.getSourceAsMap());
        }

        return list;
    }



}
