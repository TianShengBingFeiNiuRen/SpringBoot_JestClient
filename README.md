# JestClient
JestClient通过写json来实现对ElasticSearch的操作,

使用jestClient比较明显的一个优势就是，不用因为es的版本升级导致API发生改变而更改代码。

```
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.1.0.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>

	<properties>
        	<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        	<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        	<java.version>1.8</java.version>
        	<elasticsearch.version>5.6.9</elasticsearch.version>
        	<jest.version>5.3.4</jest.version>
    	</properties>
	
	<dependencies>
        	…… ……
		…… ……
        	<dependency>
            		<groupId>org.elasticsearch</groupId>
            		<artifactId>elasticsearch</artifactId>
            		<version>${elasticsearch.version}</version>
        	</dependency>
        	<dependency>
            		<groupId>io.searchbox</groupId>
            		<artifactId>jest</artifactId>
            		<version>${jest.version}</version>
        	</dependency>
    	</dependencies>
```

ESService.java跳转:
https://github.com/TianShengBingFeiNiuRen/SpringBoot_JestClient/blob/master/src/main/java/com/andon/jestclientdemo/service/ESService.java

CSDN:
https://blog.csdn.net/weixin_39792935/article/details/84328897
