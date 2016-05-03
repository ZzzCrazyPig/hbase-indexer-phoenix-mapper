
## hbase-indexer增加对phoenix数据类型映射

### 1. 背景

Phoenix中数值数据类型INTEGER、BIGINT等的序列化方式(byte[]数组)与原生HBase提供的序列化方式不一致。而hbase-indexer在往Solr同步数据从HBase获取数据是用HBase提供的序列化API工具Bytes。

**【说明】**

(1) 在morphlines的配置里面指定映射type的时候，将会使用hbase原生工具类Bytes来还原数据。

- type:byte[] copies the byte array unmodified into the record output field
- type:int converts with org.apache.hadoop.hbase.util.Bytes.toInt
- type:long converts with org.apache.hadoop.hbase.util.Bytes.toLong
- type:string converts with org.apache.hadoop.hbase.util.Bytes.toString
- type:boolean converts with org.apache.hadoop.hbase.util.Bytes.toBoolean
- type:float converts with org.apache.hadoop.hbase.util.Bytes.toFloat
- type:double converts with org.apache.hadoop.hbase.util.Bytes.toDouble
- type:short converts with org.apache.hadoop.hbase.util.Bytes.toShort
- type:bigdecimal converts with org.apache.hadoop.hbase.util.Bytes.toBigDecimal

(2) Phoenix为了解决数值类型与HBase序列化的兼容，提供了UNSIGNED_*数据类型，但是这些类型不能取负数，存在一定的限制。

所以，为了使得在Phoenix中可以创建使用普通数值类型（INTEGER、BIGINT等）的表并插入数据，同时能够通过hbase-indexer同步到Solr中，我们需要在hbase-indexer中增加对phoenix普通数值类型的支持。

### 2. 测试环境

- CDH5.4.2 5机器集群
- Phoenix4.6-HBase-1.0 CDH5.4.2适配版

### 3. 使用说明

(1) 使用如下命令打包项目

	mvn clean package

(2) 将打包后jar连同phoenix-core jar添加到CDH hbase-indexer的lib库中

- hbase-indexer-phoenix-mapper-1.0.0.jar
- phoenix-core-4.6.0-HBase-1.0.jar

放置位置:

	/usr/lib/hbase-solr/lib/

(3) morphlines.conf配置

对于定义为INTEGER、BIGINT类型的字段，需要指定type为我们自定义扩展的映射类，如下所示：

	morphlines : [
	  {
	    id : DATATYPE_TEST
	    importCommands : ["org.kitesdk.morphline.**", "com.ngdata.**"]
	
	    commands : [                   
	      {
	        extractHBaseCells {
	          mappings : [
	            // .... 其他字段映射配置
	         {
	              inputColumn : "info:col_int"
	              outputField : "col_int"
	              type : com.gosun.hbaseindexer.mapper.PhoenixIntegerMapper
	              source : value
	            }
		 {
	              inputColumn : "info:col_long"
	              outputField : "col_long"
	              type : com.gosun.hbaseindexer.mapper.PhoenixBigIntMapper
	              source : value
	            }
		 {
	              inputColumn : "info:col_string"
	              outputField : "col_string"
	              type : string
	              source : value
	            }
		  // ... 其他字段映射配置
	          ]
	        }
	      }
	      { logTrace { format : "output record: {}", args : ["@{}"] } }   
	    ]
	  } // , { more other config } , ...
	]

## hbase-indexer对phoenix salted table行键的适配

### 1. 背景

Phoenix Salted Table会在row key第一个byte里面插入一个字节，这个字节是根据row key的hash值对SALT_BUCKETS取模得到。SALT_BUCKETS的取值为1到256。由于hbase-indexer并没有适配phoenix对salted table的处理，因此当表为salted table并利用hbase-indexer往solr同步数据时，hbase-indexer还原行键值肯定会出现问题（因为rowkey第一个字节的影响）。因此，我们需要对hbase-indexer处理phoenix salted table做一个适配。

### 2.实现

通过cdh官方文档对于hbase-indexer的介绍以及hbase-indexer github上面的介绍，我们可以发现，其实对于row key的同步，也存在一定的映射处理规则。

[cdh官方文档地址](http://www.cloudera.com/documentation/archive/search/1-3-0/Cloudera-Search-User-Guide/csug_hbase_batch_indexer.html#xd_583c10bfdbd326ba--43d5fd93-1410993f8c2--7ed9__section_fbd_3rs_4k)

[hbase-indexer github地址](https://github.com/NGDATA/hbase-indexer/wiki/Indexer-configuration#unique-key-formatter)

默认情况下使用的是 `com.ngdata.hbaseindexer.uniquekey.StringUniqueKeyFormatter` 来将hbase rowkey格式化并转换到solr中。而上面提到unique-key-formatter必须实现 `com.ngdata.hbaseindexer.uniquekey.UniqueKeyFormatter`，因此如果我们要扩展自定义的rowkey格式化转换类，必须实现这个接口。也就是说我们需要通过实现一个自定义的unique-key-formatter来适配salted table行键到solr的格式化转换关系。

我们可以直接继承StringUniqueKeyFormatter并重写其中处理rowkey的方法来过滤rowkey的第一个字节即可。

代码详见`com.gosun.hbaseindexer.uniquekey.PhoenixSaltedUniqueKeyFormatter`