package com.tqmall.search.util;

import com.google.common.base.Function;
import com.tqmall.search.filter.QueryFilterUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * es 的公用类
 * Created by wcong on 14-9-9.
 */
@Slf4j
public class ElasticFactory {
    public static final String PRIMARY_KEY = "primary_key";

    public static final String highlightPreTag = "<span style = \"color:red\">";
    public static final String highlightPostTag = "</span>";
    private volatile static Client searchClient;

    private static final Integer insertPageSize = 10000;

    /**
     * 初始化 client
     * close when jvm was shut down
     *
     * @return es client
     */
    public static Client getClient() {
        if (searchClient == null) {
            synchronized (ElasticFactory.class) {
                if (searchClient == null) {
                    String[] addresses = SpringProperty.getProperty("elastic.address").split(",");
                    String clusterName = SpringProperty.getProperty("elastic.cluster.name");

                    Settings settings = Settings.settingsBuilder()
                            .put("client.transport.sniff", true) // sniff the rest of cluster so we only need to set one ip
                            .put("cluster.name", clusterName)
                            .put("client.transport.ping_timeout", "20s")
                            .put("client.transport.nodes_sampler_interval", "20s")
                            .build();
                    TransportClient transportClient = TransportClient.builder().settings(settings).build();
                    for (String address : addresses) {
                        int splitPos = address.indexOf(':');
                        String ip = address.substring(0, splitPos);
                        int port = Integer.parseInt(address.substring(splitPos + 1));
                        try {
                            transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                        log.info("ip:" + ip + " port:" + port);
                    }
                    searchClient = new Client(transportClient);
                    // close when jvm was shutdown
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        public void run() {
                            try {
                                log.info("look out ,we are going to close search client");
                                if (searchClient != null) {
                                    searchClient.close();
                                }
                            } catch (Throwable e) {
                                log.error("close searchClient have exception", e);
                            }
                        }
                    });
                }
            }
        }
        return searchClient;
    }

    // 刷新索引，make all index searchable
    public static void doRefresh(String... index) {
        if (index == null || index.length == 0) {
            return;
        }
        long startTime = System.currentTimeMillis();
        try {
            getClient().admin().indices().prepareRefresh(index).get();
            log.info("refresh index used:" + (System.currentTimeMillis() - startTime) + "ms");
        } catch (ElasticsearchException e) {
            log.error("执行索引: " + Arrays.asList(index) + " refresh操作发生异常", e);
        }
    }

    /**
     * 内部使用,合并代码
     */
    private static <T> void makeFilterBuilder(Map<String, T[]> fqMap, Function<QueryBuilder, ?> function) {
        if (fqMap != null && !fqMap.isEmpty()) {
            for (Map.Entry<String, T[]> entry : fqMap.entrySet()) {
                String field = entry.getKey();
                T[] value = entry.getValue();
                QueryBuilder filterBuilder;
                if (value.length > 1) {
                    filterBuilder = QueryBuilders.termsQuery(field, value);
                } else if (value.length == 1) {
                    filterBuilder = QueryBuilders.termQuery(field, value[0]);
                } else {
                    continue;
                }
                function.apply(filterBuilder);
            }
        }
    }

    /**
     * 基本搜索
     * 条件之间AND逻辑
     *
     * @param indexNameType 索引名
     * @param fqMap         筛选
     * @param rFields       返回的字段
     * @param <T>           筛选的值
     * @return 搜索的结果
     */
    public static <T> SearchHit[] commonSearch(IndexNameType indexNameType, Map<String, T[]> fqMap, String[] rFields) {
        final BoolQueryBuilder andFilterBuilder = QueryBuilders.boolQuery();
        makeFilterBuilder(fqMap, new Function<QueryBuilder, Object>() {
            @Override
            public Object apply(QueryBuilder filterBuilder) {
                andFilterBuilder.must(filterBuilder);
                return null;
            }
        });
        return filterSearch(indexNameType, null, andFilterBuilder, rFields, null);
    }

    public static <T> SearchHit[] querySearch(IndexNameType indexNameType,
                                              Map<String, T[]> fqMap, String[] rFields, QueryBuilder queryBuilder, SortBuilder sortBuilder, int limit) {
        final BoolQueryBuilder andFilterBuilder = QueryBuilders.boolQuery();
        makeFilterBuilder(fqMap, new Function<QueryBuilder, Object>() {
            @Override
            public Object apply(QueryBuilder filterBuilder) {
                andFilterBuilder.must(filterBuilder);
                return null;
            }
        });
        if (limit <= 0) {
            return filterSearch(indexNameType, queryBuilder, andFilterBuilder, rFields, sortBuilder);
        } else {
            return filterSearch(indexNameType, queryBuilder, andFilterBuilder, rFields, sortBuilder, limit);
        }
    }

    /**
     * query搜索
     * 条件之间AND逻辑
     *
     * @param indexNameType 索引名
     * @param fqMap         筛选
     * @param rFields       返回的字段
     * @param <T>           筛选的值
     * @return 搜索的结果
     */
    public static <T> SearchHit[] querySearch(IndexNameType indexNameType,
                                              Map<String, T[]> fqMap, String[] rFields, QueryBuilder queryBuilder) {
        return querySearch(indexNameType, fqMap, rFields, queryBuilder, null, 0);
    }

    /**
     * 基本搜索
     * 条件之间OR逻辑
     *
     * @param indexNameType 索引名
     * @param fqMap         筛选
     * @param rFields       返回的字段
     * @param <T>           筛选的值
     * @return 搜索的结果
     */
    public static <T> SearchHit[] commonOrSearch(IndexNameType indexNameType, Map<String, T[]> fqMap, String[] rFields) {
        final BoolQueryBuilder orFilterBuilder = QueryBuilders.boolQuery();
        makeFilterBuilder(fqMap, new Function<QueryBuilder, Object>() {
            @Override
            public Object apply(QueryBuilder filterBuilder) {
                orFilterBuilder.should(filterBuilder);
                return null;
            }
        });
        return filterSearch(indexNameType, null, orFilterBuilder, rFields, null);
    }

    /**
     * 基本搜索
     * 条件之间AND逻辑
     *
     * @param indexNameType 索引名
     * @param filterBuilder 筛选
     * @param rFields       返回的字段
     * @return 搜索的结果
     */
    public static SearchHit[] filterSearch(IndexNameType indexNameType,
                                           QueryBuilder queryBuilder, QueryBuilder filterBuilder, String[] rFields) {
        return filterSearch(new IndexNameType[]{indexNameType}, queryBuilder, filterBuilder, rFields, null);
    }

    /**
     * 基本搜索
     * 条件之间AND逻辑
     *
     * @param indexNameType 索引名
     * @param filterBuilder 筛选
     * @param rFields       返回的字段
     * @return 搜索的结果
     */
    public static SearchHit[] filterSearch(IndexNameType indexNameType,
                                           QueryBuilder queryBuilder, QueryBuilder filterBuilder, String[] rFields, SortBuilder sortBuilder) {
        return filterSearch(new IndexNameType[]{indexNameType}, queryBuilder, filterBuilder, rFields, sortBuilder);
    }

    /**
     * 可查多个索引
     *
     * @param indexNameTypes 多个索引名
     * @param queryBuilder   查询条件
     * @param filterBuilder  后置过滤条件
     * @param rFields        返回列
     * @param sortBuilder    此处查询结果较多,且在大多数情况下不需要排序,故此参数暂时不使用,如果需要请调用{@link #filterSearch(IndexNameType[], QueryBuilder, QueryBuilder, String[], SortBuilder, int)}
     */
    public static SearchHit[] filterSearch(IndexNameType[] indexNameTypes,
                                           QueryBuilder queryBuilder, QueryBuilder filterBuilder, String[] rFields, SortBuilder sortBuilder) {
        Set<String> index = new HashSet<>();
        Set<String> type = new HashSet<>();
        for (IndexNameType indexNameType : indexNameTypes) {
            index.add(indexNameType.getIndexName());
            type.add(indexNameType.getIndexType());
        }
        Long startTime = new Date().getTime();
        SearchRequestBuilder searchRequestBuilder = ElasticFactory.getClient()
                .prepareSearch(indexNameTypes)
                .setSearchType(SearchType.SCAN)
                .setScroll("1m")
                .setSize(100000);
        //此处无聚类 原postFilter修改为 constant score query
        BoolQueryBuilder boolQueryBuilder = QueryFilterUtils.wrapQueryAndPostFilter(queryBuilder, filterBuilder);

        if (boolQueryBuilder.hasClauses()) {
            searchRequestBuilder.setQuery(boolQueryBuilder);
        }
        if (rFields != null) {
            searchRequestBuilder.addFields(rFields);
        }
        List<SearchHit> retList = new LinkedList<>();
        int num = 0;
        SearchResponse scrollResponse = searchRequestBuilder.execute().actionGet();
        String scrollId = scrollResponse.getScrollId();
        if (scrollId != null && scrollId.length() > 0) {
            SearchHit[] searchRet;
            do {
                SearchResponse searchResponse = ElasticFactory.getClient()
                        .prepareSearchScroll(scrollId)
                        .setScroll("1m")
                        .execute().actionGet();
                scrollId = searchResponse.getScrollId();
                searchRet = searchResponse.getHits().getHits();
                Collections.addAll(retList, searchRet);
                num += searchRet.length;
            } while (searchRet.length != 0);
        }
        Long endTime = new Date().getTime();
        log.info("search index:" + ListUtil.implode(index, ",") + " type:" + ListUtil.implode(type, ",") + " num:" + num + " used:" + (endTime - startTime) + "ms");
        SearchHit[] retSearchArr = new SearchHit[retList.size()];
        return retList.toArray(retSearchArr);
    }

    /**
     * 基本搜索
     * 条件之间AND逻辑
     *
     * @param indexNameType 索引名
     * @param filterBuilder 筛选
     * @param rFields       返回的字段
     * @param <T>           筛选的值
     * @param limit         这不是分页，只是限制只返回几条
     * @return 搜索的结果
     */
    public static <T> SearchHit[] filterSearch(IndexNameType indexNameType,
                                               QueryBuilder queryBuilder, QueryBuilder filterBuilder, String[] rFields, SortBuilder sortBuilder, int limit) {
        return filterSearch(new IndexNameType[]{indexNameType}, queryBuilder, filterBuilder, rFields, sortBuilder, limit);
    }

    /**
     * 基本搜索
     * 条件之间AND逻辑
     *
     * @param indexNameTypes 索引名
     * @param filterBuilder  筛选
     * @param rFields        返回的字段
     * @param <T>            筛选的值
     * @param limit          这不是分页，只是限制只返回几条
     * @return 搜索的结果
     */
    public static <T> SearchHit[] filterSearch(IndexNameType[] indexNameTypes,
                                               QueryBuilder queryBuilder, QueryBuilder filterBuilder, String[] rFields, SortBuilder sortBuilder, int limit) {
        Set<String> index = new HashSet<>();
        Set<String> type = new HashSet<>();
        for (IndexNameType indexNameType : indexNameTypes) {
            index.add(indexNameType.getIndexName());
            type.add(indexNameType.getIndexType());
        }
        Long startTime = new Date().getTime();
        SearchRequestBuilder searchRequestBuilder = ElasticFactory.getClient()
                .prepareSearch(indexNameTypes)
                .setSize(limit);

        if (sortBuilder != null)
            searchRequestBuilder.addSort(sortBuilder);

        //此处无聚类 原postFilter修改为 constant score query
        BoolQueryBuilder boolQueryBuilder = QueryFilterUtils.wrapQueryAndPostFilter(queryBuilder, filterBuilder);

        if (boolQueryBuilder.hasClauses()) {
            searchRequestBuilder.setQuery(boolQueryBuilder);
        }
        if (rFields != null) {
            searchRequestBuilder.addFields(rFields);
        }
        List<SearchHit> retList = new LinkedList<>();
        retList.addAll(Arrays.asList(searchRequestBuilder.execute().actionGet().getHits().getHits()));
        int num = 0;
        Long endTime = new Date().getTime();
        log.info("search index:" + ListUtil.implode(index, ",") + " type:" + ListUtil.implode(type, ",") + " num:" + num + " used:" + (endTime - startTime) + "ms");
        SearchHit[] retSearchArr = new SearchHit[retList.size()];
        return retList.toArray(retSearchArr);
    }

    /**
     * 插入数据
     *
     * @param indexNameType 索引
     * @param xMap          数据内容
     * @param refresh       刷新索引
     * @return 结果
     */
    private static boolean _bulkInsertData(IndexNameType indexNameType, Map<String, XContentBuilder> xMap, boolean refresh) {
        if (xMap.size() <= 0) {
            return false;
        }
        try {
            BulkRequestBuilder bulkRequest = getClient().prepareBulk().setRefresh(refresh);
            for (Map.Entry<String, XContentBuilder> entry : xMap.entrySet()) {
                bulkRequest.add(getClient().prepareIndex(indexNameType, entry.getKey()).setSource(entry.getValue()));
            }
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (!bulkResponse.hasFailures()) {
                return true;
            } else {
                log.error(bulkResponse.buildFailureMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 快速插入数据,不刷新索引
     *
     * @param indexNameType 索引
     * @param indexField    唯一标示
     * @param docList       内容
     * @return 结果
     */
    public static boolean bulkInsertData(IndexNameType indexNameType, String indexField, List<Map<String, Object>> docList) {
        if (docList != null && !docList.isEmpty()) {
            List<List<Map<String, Object>>> chunkList = ListUtil.arrayChunk(docList, insertPageSize);
            for (List<Map<String, Object>> insertContentList : chunkList) {
                Map<String, XContentBuilder> xMap = makeXContent(indexField, insertContentList);
                // 执行插入
                _bulkInsertData(indexNameType, xMap, false);
            }
        }
        return true;
    }

    /**
     * 插入数据,刷新索引（速度较慢）
     *
     * @param indexNameType 索引
     * @param indexField    唯一标示
     * @param docList       内容
     * @return 结果
     */
    public static boolean bulkInsertDataAndRefresh(IndexNameType indexNameType, String indexField, List<Map<String, Object>> docList) {
        if (docList != null && !docList.isEmpty()) {
            List<List<Map<String, Object>>> chunkList = ListUtil.arrayChunk(docList, insertPageSize);
            for (List<Map<String, Object>> insertContentList : chunkList) {
                Map<String, XContentBuilder> xMap = makeXContent(indexField, insertContentList);
                // 执行插入
                _bulkInsertData(indexNameType, xMap, true);
            }
        }
        return true;
    }

    private static Map<String, XContentBuilder> makeXContent(String indexField, List<Map<String, Object>> contentList) {
        Map<String, XContentBuilder> xMap = new HashMap<String, XContentBuilder>();
        for (Map<String, Object> c : contentList) {
            XContentBuilder xContentBuilder;
            try {
                xContentBuilder = XContentFactory.jsonBuilder().startObject();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return null;
            }
            for (Map.Entry<String, Object> entry : c.entrySet()) {
                if (entry.getKey().equals(ElasticFactory.PRIMARY_KEY)) {
                    continue;
                }
                String field = entry.getKey();
                Object values = entry.getValue();
                try {
                    xContentBuilder.field(field, values);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                xContentBuilder.endObject();
            } catch (IOException e) {
                e.printStackTrace();
            }
            xMap.put(String.valueOf(c.get(indexField)), xContentBuilder);
        }
        return xMap;
    }

    /**
     * 快速批量删除索引，不刷新
     *
     * @param indexNameType 索引
     * @param idList        id 列表
     * @return 结果
     */
    public static boolean bulkDeleteData(IndexNameType indexNameType, List<String> idList) {
        try {
            if (!idList.isEmpty()) {
                List<List<String>> chunkList = ListUtil.arrayChunk(idList, insertPageSize);
                for (List<String> innerList : chunkList) {
                    BulkRequestBuilder bulkRequest = getClient().prepareBulk();
                    for (String id : innerList) {
                        bulkRequest.add(getClient().prepareDelete(indexNameType, id));
                    }
                    BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                    if (bulkResponse.hasFailures()) {
                        log.error(bulkResponse.buildFailureMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 批量删除索引，刷新索引（较慢）
     *
     * @param indexNameType 索引
     * @param idList        id 列表
     * @return 结果
     */
    public static boolean bulkDeleteDataAndRefresh(IndexNameType indexNameType, List<String> idList) {
        try {
            if (!idList.isEmpty()) {
                List<List<String>> chunkList = ListUtil.arrayChunk(idList, insertPageSize);
                for (List<String> innerList : chunkList) {
                    BulkRequestBuilder bulkRequest = getClient().prepareBulk().setRefresh(true);
                    for (String id : innerList) {
                        bulkRequest.add(getClient().prepareDelete(indexNameType, id));
                    }
                    BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                    if (bulkResponse.hasFailures()) {
                        log.error(bulkResponse.buildFailureMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除类型
     *
     * @param indexNameType 索引名称
     * @return 结果
     */
    public static boolean deleteType(IndexNameType indexNameType) {
        try {
            IndicesExistsResponse indicesExistsResponse = getClient().admin().indices()
                    .prepareExists(indexNameType.getIndexName()).execute().actionGet();
            if (indicesExistsResponse.isExists()) {
                TypesExistsResponse response = getClient().admin().indices()
                        .prepareTypesExists(indexNameType.getIndexName()).setTypes(indexNameType.getIndexType())
                        .execute().actionGet();
                if (response.isExists()) {
                    // 删除已有index,整个index直接删除,不能单独删除type
                    DeleteIndexRequest deleteMapping = Requests.deleteIndexRequest(indexNameType.getIndexName());
                    getClient().admin().indices().delete(deleteMapping).actionGet();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 如果索引中又日期字段，新建索引前需要调用该方法
     *
     * @param indexNameType 索引,创建索引都使用真实的名字称
     */
    public static void createMapping(IndexNameType indexNameType, XContentBuilder builder) {
        try {
            // 先创建索引
            IndicesExistsResponse indicesExistsResponse = getClient().admin().indices()
                    .prepareExists(indexNameType.getIndexRealName()).execute().actionGet();
            if (indicesExistsResponse.isExists()) {
                TypesExistsResponse response = getClient().admin().indices()
                        .prepareTypesExists(indexNameType.getIndexRealName()).setTypes(indexNameType.getIndexType())
                        .execute().actionGet();
                if (response.isExists()) {
                    return;
                }
            }

            // 创建index和type
            getClient().admin().indices().prepareCreate(indexNameType.getIndexRealName()).execute().actionGet();

            // 创建mapping
            PutMappingRequest mapping = Requests.putMappingRequest(indexNameType.getIndexRealName()).type(indexNameType.getIndexType()).source(builder);
            getClient().admin().indices().putMapping(mapping).actionGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
