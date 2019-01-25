package com.andon.jestclientdemo.service;

import com.andon.jestclientdemo.domain.KLine;
import com.andon.jestclientdemo.domain.MacdResonance;
import com.andon.jestclientdemo.util.GsonUtil;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class ESService {

    private static final Logger LOG = LoggerFactory.getLogger(ESService.class);
    private static final ThreadLocal<DateFormat> format = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    @Resource
    JestClient jestClient;

    @Value("${kline_index}")
    private String indexName;
    @Value("${kline_type}")
    private String typeName;
    @Value("${quote1}")
    private String BTC;
    @Value("${quote2}")
    private String USDT;
    @Value("${es.index.macdResonance}")
    private String macdResonanceIndex;

    /**
     * 获取doc
     */
    public KLine getKlineDataById(String index, String type, String id) {
        Get get = new Get.Builder(index, id).type(type).build();
        try {
            DocumentResult documentResult = jestClient.execute(get);
            return documentResult.getSourceAsObject(KLine.class);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.warn("getKlineDataById again!! error={}", e.getMessage());
            return getKlineDataById(index, type, id);
        }
    }

    /**
     * 插入或更新
     */
    public void insertOrUpdateKLineData(KLine kline, String id, String index, String type) {
        Index.Builder builder = new Index.Builder(kline).id(id).refresh(true);
        Index indexDoc = builder.index(index).type(type).build();
        try {
            DocumentResult result = jestClient.execute(indexDoc);
            LOG.info("status:" + result.isSucceeded() + " index:" + result.getIndex() + " type:" + result.getType() + " id:" + result.getId());
        } catch (IOException e) {
            LOG.warn("insertOrUpdateKLineData again!! error={}", e.getMessage());
            insertOrUpdateKLineData(kline, id, index, type);
        }
    }

    /**
     * json查询
     */
    private SearchResult jsonSearch(String json, String indexName, String typeName) {
        Search search = new Search.Builder(json).addIndex(indexName).addType(typeName).build();
        try {
            return jestClient.execute(search);
        } catch (Exception e) {
            LOG.warn("index:{}, type:{}, search again!! error = {}", indexName, typeName, e.getMessage());
            try {
                Thread.sleep(1);
            } catch (InterruptedException e1) {
                LOG.error(e.getMessage(), e);
            }
            return jsonSearch(json, indexName, typeName);
        }
    }

    /**
     * 查询kline(根据时间)
     */
    public KLine searchKline(String base, String quote, String startTime, String endTime) {
        String json = "{\n" +
                "\"size\": 1,\n" +
                "\"query\": {\n" +
                "   \"bool\": {\n" +
                "       \"must\": [\n" +
                "           { \"match\": { \"base\": \"" + base + "\" } },\n" +
                "           { \"match\": { \"quote\": \"" + quote + "\" } },\n" +
                "           { \"range\": { \"time\": {\n" +
                "                               \"gte\": \"" + startTime + "\",\n" +
                "                               \"lt\": \"" + endTime + "\"\n" +
                "                       }\n" +
                "                   }" +
                "               }\n" +
                "           ]\n" +
                "       }\n" +
                "   },\n" +
                "\"sort\": {\n" +
                "   \"time\": {\n" +
                "       \"order\": \"desc\"\n" +
                "       }\n" +
                "   }\n" +
                "}";
        // 通过json查询
        SearchResult search = jsonSearch(json, indexName, typeName);
        KLine kLine = search.getSourceAsObject(KLine.class, false);
        LOG.info("base={}, quote={}, startTime={}, endTime={}, Kline = {}", base, quote, startTime, endTime, kLine);
        return kLine;
    }

    /**
     * 多条件组合JSON查询
     */
    public String searchMacdResonance(String startTime, String endTime, int pageNum, int pageSize, String domain, String pair, String quote, String macdType, String macdTimeType) {
        String index = macdResonanceIndex;
        String type = macdResonanceIndex;
        /*String startTime = "1979-01-01 00:00:00";
        String endTime = "1979-02-01 00:00:00";
        int pageNum = 1;
        int pageSize = 12;

        String domain = "www.binance.com";
        String base = "BTC";
        String quote = "USDT";
        String pair = base + "-" + quote;
        String macdType = "底背离,顶背离";
        String macdTimeType = "5min,15min,30min";*/

        String domainJson = "";
        if (!ObjectUtils.isEmpty(domain)) {
            domainJson = "{ \"match\": { \"domain\": \"" + domain + "\" } },\n";
        }
        String pairJson = "";
        if (!ObjectUtils.isEmpty(pair)) {
            pairJson = "{ \"match\": { \"pair\": \"" + pair + "\" } },\n";
        }
        String quoteJson = "";
        if (!ObjectUtils.isEmpty(quote)) {
            quoteJson = "{ \"match\": { \"quote\": \"" + quote + "\" } },\n";
        }
        String macdTypeMustJson = "";
        if (!ObjectUtils.isEmpty(macdType) && macdType.split(",").length == 1) {
            macdTypeMustJson = "{ \"match\": { \"macdType\": \"" + macdType + "\" } },\n";
        }
        StringBuilder macdTypeShouldJson = new StringBuilder();
        if (!ObjectUtils.isEmpty(macdType) && macdType.split(",").length != 1) {
            String[] split = macdType.split(",");
            for (int i = 0; i < split.length; i++) {
                if (i == 0) {
                    macdTypeShouldJson.append("{ \"match\": { \"macdType\": \"").append(split[i]).append("\" } }\n");
                } else {
                    macdTypeShouldJson.append(",{ \"match\": { \"macdType\": \"").append(split[i]).append("\" } }\n");
                }
            }
        }
        String macdTimeTypeMustJson = "";
        if (!ObjectUtils.isEmpty(macdTimeType) && macdTimeType.split(",").length == 1) {
            macdTimeTypeMustJson = "{ \"match\": { \"macdTimeType\": \"" + macdTimeType + "\" } },\n";
        }
        StringBuilder macdTimeTypeShouldJson = new StringBuilder();
        if (!ObjectUtils.isEmpty(macdTimeType) && macdType.split(",").length != 1) {
            String[] split = macdTimeType.split(",");
            for (int i = 0; i < split.length; i++) {
                if (i == 0) {
                    macdTimeTypeShouldJson.append("{ \"match\": { \"macdTimeType\": \"").append(split[i]).append("\" } }\n");
                } else {
                    macdTimeTypeShouldJson.append(",{ \"match\": { \"macdTimeType\": \"").append(split[i]).append("\" } }\n");
                }
            }
        }
        String json = "{\n" +
                "\"from\": " + (pageNum - 1) + ",\n" +
                "\"size\": " + pageSize + ",\n" +
                "\"query\": {\n" +
                "   \"bool\": {\n" +
                "       \"must\": [\n" +
                "           " + domainJson +
                "           " + pairJson +
                "           " + quoteJson +
                "           " + macdTypeMustJson +
                "           " + macdTimeTypeMustJson +
                "           { \"range\": { \"time\": {\n" +
                "                               \"gte\": \"" + startTime + "\",\n" +
                "                               \"lt\": \"" + endTime + "\"\n" +
                "                       }\n" +
                "                   }" +
                "               }\n" +
                "           ]\n," +
                "       \"should\": [" +
                "           " + macdTypeShouldJson +
                "           ], " +
                "       \"should\": [" +
                "           " + macdTimeTypeShouldJson +
                "           ]" +
                "       }\n" +
                "   },\n" +
                "\"sort\": {\n" +
                "   \"time\": {\n" +
                "       \"order\": \"desc\"\n" +
                "       }\n" +
                "   }\n" +
                "}";
        SearchResult searchResult = jsonSearch(json, index, type);
        List<MacdResonance> sourceAsObjectList = searchResult.getSourceAsObjectList(MacdResonance.class, true);
        return GsonUtil.GSON.toJson(sourceAsObjectList);
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
            if (jestResult != null) {
                return jestResult.isSucceeded();
            }
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
            if (jestResult != null) {
                return jestResult.isSucceeded();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
