package com.andon.jestclientdemo;

import com.andon.jestclientdemo.domain.KLine;
import com.andon.jestclientdemo.service.ESService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ESServiceTest {

    @Value("${kline_index}")
    private String indexName;
    @Value("${kline_type}")
    private String typeName;
    @Resource
    private ESService esService;

    @Test
    public void getKlineDataByIdTest(){
        String id = "";
        KLine kLine = esService.getKlineDataById(indexName, typeName, id);
        System.out.println(kLine);
    }

    @Test
    public void test(){
        KLine kLine = new KLine();
        String id = "";
        esService.insertOrUpdateKLineData(kLine, id, indexName, typeName);
    }
}
