package com.andon.jestclientdemo.service;

import com.andon.jestclientdemo.domain.BaseModel;
import com.google.gson.JsonObject;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.*;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.mapping.GetMapping;
import io.searchbox.indices.mapping.PutMapping;
import io.searchbox.indices.settings.GetSettings;
import io.searchbox.indices.settings.UpdateSettings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Service
public class ESService {

    private static final Logger LOG = LoggerFactory.getLogger(ESService.class);

    private JestClient jestClient;
    @Value("${uris}")
    private String url;

    @PostConstruct
    public void init() {
        List<String> uris = Arrays.asList(url.split(","));
        JestClientFactory jestClientFactory = new JestClientFactory();
        jestClientFactory.setHttpClientConfig(new HttpClientConfig
                .Builder(uris)
                .connTimeout(10000)
                .readTimeout(10000)
                .multiThreaded(true)
                .build());
        jestClient = jestClientFactory.getObject();
    }

    /**
     * 发送json查询
     */
    SearchResult jsonSearch(String json, String indexName, String typeName) {
        Search search = new Search.Builder(json).addIndex(indexName).addType(typeName).build();
        try {
            return jestClient.execute(search);
        } catch (Exception e) {
            LOG.warn("index:{}, type:{}, search again!! error = {}", indexName, typeName, e.getMessage());
            sleep(100);
            return jsonSearch(json, indexName, typeName);
        }
    }

    /**
     * 批量写入
     */
    public <T extends BaseModel> void bulkIndex(List<T> list, String indexName) {
        Bulk.Builder bulk = new Bulk.Builder();
        for (T o : list) {
            Index index = new Index.Builder(o).id(o.getPK()).index(indexName).type(o.getType()).build();
            bulk.addAction(index);
        }
        try {
            jestClient.execute(bulk.build());
        } catch (IOException e) {
            LOG.warn("bulkIndex again!! error={} index={}", e.getMessage(), indexName);
            sleep(100);
            bulkIndex(list, indexName);
        }
    }

    /**
     * 新增或者更新文档
     */
    public <T> void insertOrUpdateDocumentById(T o, String index, String type, String uniqueId) {
        Index.Builder builder = new Index.Builder(o);
        builder.id(uniqueId);
        builder.refresh(true);
        Index indexDoc = builder.index(index).type(type).build();
        try {
            jestClient.execute(indexDoc);
        } catch (IOException e) {
            LOG.warn("insertOrUpdateDocumentById again!! error={} id={}", e.getMessage(), uniqueId);
            sleep(100);
            insertOrUpdateDocumentById(o, index, type, uniqueId);
        }
    }

    /**
     * 根据主键id删除文档
     */
    public void deleteDocumentById(String index, String type, String id) {
        Delete delete = new Delete.Builder(id).index(index).type(type).build();
        try {
            jestClient.execute(delete);
        } catch (IOException e) {
            LOG.warn("deleteDocumentById again!! error={} id={}", e.getMessage(), id);
            sleep(100);
            deleteDocumentById(index, type, id);
        }
    }

    /**
     * 根据主键id获取文档
     */
    public <T> T getDocumentById(T object, String index, String id) {
        Get get = new Get.Builder(index, id).build();
        T o = null;
        try {
            JestResult result = jestClient.execute(get);
            o = (T) result.getSourceAsObject(object.getClass());
        } catch (IOException e) {
            LOG.warn("getDocumentById again!! error={} id=");
            sleep(100);
            getDocumentById(object, index, id);
        }
        return o;
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            LOG.error("Thread sleep failure!! error={}", e.getMessage());
        }
    }

    /**
     * 创建index
     */
    public void createIndex(String index) {
        try {
            JestResult jestResult = jestClient.execute(new CreateIndex.Builder(index).build());
            System.out.println("createIndex:{}" + jestResult.isSucceeded());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除index
     */
    public void deleteIndex(String index) {
        try {
            JestResult jestResult = jestClient.execute(new DeleteIndex.Builder(index).build());
            System.out.println("deleteIndex result:{}" + jestResult.isSucceeded());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置index的mapping（设置数据类型和分词方式）
     */
    public void createIndexMapping(String index, String type, String mappingString) {
        // mappingString为拼接好的json格式的mapping串
        PutMapping.Builder builder = new PutMapping.Builder(index, type, mappingString);
        try {
            JestResult jestResult = jestClient.execute(builder.build());
            System.out.println("createIndexMapping result:{}" + jestResult.isSucceeded());
            if (!jestResult.isSucceeded()) {
                System.err.println("settingIndexMapping error:{}" + jestResult.getErrorMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取index的mapping
     */
    public String getMapping(String indexName, String typeName) {
        GetMapping.Builder builder = new GetMapping.Builder();
        builder.addIndex(indexName).addType(typeName);
        try {
            JestResult result = jestClient.execute(builder.build());
            return result.getSourceAsObject(JsonObject.class).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取索引index设置setting
     */
    public boolean getIndexSettings(String index) {
        try {
            JestResult jestResult = jestClient.execute(new GetSettings.Builder().addIndex(index).build());
            System.out.println(jestResult.getJsonString());
            return jestResult.isSucceeded();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更改索引index设置setting
     */
    public boolean updateIndexSettings(String index) {
        String source;
        XContentBuilder mapBuilder = null;
        try {
            mapBuilder = XContentFactory.jsonBuilder();
            mapBuilder.startObject().startObject("index").field("max_result_window", "1000000").endObject().endObject();
            source = mapBuilder.string();
            JestResult jestResult = jestClient.execute(new UpdateSettings.Builder(source).build());
            System.out.println(jestResult.getJsonString());
            return jestResult.isSucceeded();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
