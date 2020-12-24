package com.ldh;

import com.alibaba.fastjson.JSON;
import com.ldh.domain.User;
import com.ldh.utils.ElasticSearchConstant;
import org.apache.lucene.search.Query;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 讲解 ElasticSearch 7.10.1 API
 */
@SpringBootTest
class KuangshenEsApiApplicationTests {

	//自动注入
/*	@Autowired
	private RestHighLevelClient restHighLevelClient;*/

	//面向对象来操作
	@Autowired
	@Qualifier("restHighLevelClient")
	private RestHighLevelClient client;

	//测试索引的创建 Request
	@Test
	void testCreatIndex() throws IOException {
		//1.创建索引请求
		CreateIndexRequest request=new CreateIndexRequest("kuang_index");
		//2.客户端执行请求 indicesClient,请求后获的响应
		CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

		System.out.println(createIndexResponse);
	}

	//测试获取索引
	@Test
	void testExistIndex() throws IOException {
		//获取索引请求
		GetIndexRequest request=new GetIndexRequest("kuang_index");
		//判断索引是否存在
		boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);

		System.out.println(exists);
	}

	//测试删除索引
	@Test
	void testDeleteIndex() throws IOException {
		//删除索引请求
		DeleteIndexRequest request=new DeleteIndexRequest("kuang_index");
		//删除
		AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);

		System.out.println(delete);
		System.out.println(delete.isAcknowledged());
	}

	//测试创建文档
	@Test
	void testAddDocument() throws IOException {
		//创建User对象
		User user = new User("ElasticSearch", 25, "狂神说Java");
		//创建文档请求
		IndexRequest request=new IndexRequest("kuang_index");
		//规则 PUT /kuang_index/_doc/1
		request.id("2");
		request.timeout("2s");
		//request.timeout(TimeValue.timeValueSeconds(2));
		//将我们的数据放入请求 json
		request.source(JSON.toJSONString(user), XContentType.JSON);

		//客户端发送请求,获取响应的结果
		IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

		System.out.println(indexResponse.toString());
		//对应我们命令返回的状态
		System.out.println(indexResponse.status());
		//获取版本号
		System.out.println(indexResponse.getVersion());
	}

	//获取文档，判断是否存在
	@Test
	void testIsExistDocument() throws IOException {
		// GET /kuang_index/_doc/1
		//创建Get 请求
		GetRequest getRequest = new GetRequest("kuang_index", "1");
		//不获取返回的 _source 的上下文
		getRequest.fetchSourceContext(new FetchSourceContext(false));
		//排序
		getRequest.storedFields("_none_");

		//判断是否存在
		boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
		System.out.println(exists);
		System.out.println(getRequest.toString());
	}

	//获取文档信息
	@Test
	void testGetDocument() throws IOException {
		// GET /kuang_index/_doc/1
		//创建Get 请求
		GetRequest getRequest = new GetRequest("kuang_index", "1");

		//获取文档信息
		GetResponse documentFields = client.get(getRequest, RequestOptions.DEFAULT);

		System.out.println(documentFields);
		System.out.println(documentFields.getSourceAsString());
		System.out.println(documentFields.getIndex());
		System.out.println(documentFields.getField(""));

	}

	//更新文档信息
	@Test
	void testUpdateDocument() throws IOException {
		// GET /kuang_index/_doc/1
		//创建Get 请求
		UpdateRequest updateRequest = new UpdateRequest("kuang_index", "1");
		updateRequest.timeout("1s");

		//创建User对象
		User user = new User("ElasticSearch搜索引擎", 23, "狂神说Java");
		//将我们的数据放入请求 json
		updateRequest.doc(JSON.toJSONString(user), XContentType.JSON);
		//发送更新文档信息请求
		UpdateResponse update = client.update(updateRequest, RequestOptions.DEFAULT);

		System.out.println(update);
		System.out.println(update.status());
	}

	//删除文档记录
	@Test
	void testDeleteDocument() throws IOException {
		//创建Delete请求
		DeleteRequest deleteRequest = new DeleteRequest("kuang_index", "3");

		//deleteRequest.timeout(TimeValue.timeValueSeconds(1));
		deleteRequest.timeout("1s");
		//发送删除文档记录请求
		DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);

		if (deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {
			System.out.println("记录不存在，删除文档记录失败");
			//throw new IOException("删除文档记录失败");
		}
		System.out.println(deleteResponse.status());
	}

	//特殊的，真实项目一般都会批量插入数据
	@Test
	void testBulkRequest() throws IOException {
		//创建BulkRequest请求
		BulkRequest bulkRequest=new BulkRequest();
		bulkRequest.timeout("10s");

		ArrayList<User> userList=new ArrayList<>();
		userList.add(new User("廉颇", 45, "廉颇老矣,尚能饭否!!"));
		userList.add(new User("关羽", 42, "五虎上将之大哥"));
		userList.add(new User("张飞", 43, "冲动、鲁莽、有勇无谋"));
		userList.add(new User("赵云", 39, "一夫当关万夫莫开"));

		//批处理请求
		for (int i=0;i<userList.size();i++) {
			//批量更新和批量删除，就在这里修改对应的请求即可
			//id覆盖问题（若id已经存在，则会覆盖）
			/*bulkRequest.add(new IndexRequest("kuang_index")
					.id(""+(i+1)).source(JSON.toJSONString(userList.get(i)),XContentType.JSON));*/
			//不填id,则系统会产生随机id
			/*bulkRequest.add(new IndexRequest("kuang_index")
					.source(JSON.toJSONString(userList.get(i)),XContentType.JSON));*/
			//真实项目中，索引库一般创建一个类枚举出来
			bulkRequest.add(new IndexRequest(ElasticSearchConstant.ES_INDEX)
					.source(JSON.toJSONString(userList.get(i)),XContentType.JSON));
		}
		//发送请求
		BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
		//是否执行失败---返回false:执行成功
		System.out.println(bulkResponse.hasFailures());
	}



	/**
	 * @deprecated 查询某个库所有数据
	 * @throws IOException
	 *
	 *  SearchRequest 搜索请求
	 *  SearchSourceBuilder 条件构造器
	 *  HighlightBuilder 高亮显示
	 *  TermQueryBuilder 精确查询
	 *  MatchAllQueryBuilder 查询匹配所有
	 */
	@Test
	void testSearchRequest() throws IOException {
		//创建SearchRequest请求
		SearchRequest searchRequest=new SearchRequest(ElasticSearchConstant.ES_INDEX);

		//构建搜索条件
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		//查询条件，可以使用QueryBuilders 工具类实现
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		ExistsQueryBuilder name = QueryBuilders.existsQuery("name");
		//查询匹配所有
		MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
		//匹配查询
		MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", "廉颇");
		//精确查询
		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "廉");

		//查询
		sourceBuilder.query(matchAllQueryBuilder);
		//设置超时
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		//高亮显示
		sourceBuilder.highlighter();
		//分页
		//sourceBuilder.from(0);
		//sourceBuilder.size(5);
		//放入构建请求中
		searchRequest.source(sourceBuilder);
		//响应结果
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		System.out.println(searchResponse.status());
		//System.out.println(JSON.toJSONString(searchResponse.getHits()));
		for (SearchHit hit : searchResponse.getHits().getHits()) {
			System.out.println(hit.getSourceAsMap());
		}

	}

}
