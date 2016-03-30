package com.tqmall.search.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesAction;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesAction;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexAction;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.index.IndexNotFoundException;

import java.util.*;

/**
 * Created by 刘一波 on 16/3/22.
 * E-Mail:yibo.liu@tqmall.com
 */
@Slf4j
public class ElasticAliasQuery {
    /**
     * 根据别名获取索引名，返回 GetAliasesResponse 对象
     *
     * @param indexAlias
     * @return
     */
    public static GetAliasesResponse getIndexByAlias(String indexAlias) {
        if (StringUtils.isNotBlank(indexAlias)) {
            GetAliasesRequestBuilder getAliasesRequestBuilder = GetAliasesAction.INSTANCE.newRequestBuilder(ElasticFactory.getClient());
            GetAliasesResponse getAliasesResponse = getAliasesRequestBuilder.addAliases(indexAlias).get();
            return getAliasesResponse;
        }
        return null;
    }

    /**
     * 根据别名获取索引名，返回数组
     */
    public static String[] getIndexesByAlias(String indexAlias) {
        if (StringUtils.isNotBlank(indexAlias)) {
            GetAliasesRequestBuilder getAliasesRequestBuilder = GetAliasesAction.INSTANCE.newRequestBuilder(ElasticFactory.getClient());
            return getAliasesRequestBuilder.addAliases(indexAlias).get().getAliases().keys().toArray(String.class);
        }
        return new String[0];
    }

    /**
     * 根据索引别名删除所有索引
     *
     * @param indexAlias
     */
    public static void removeAllIndexByAlias(String indexAlias) {
        if (StringUtils.isNotBlank(indexAlias)) {
            String[] indexNames = getIndexesByAlias(indexAlias);
            for (String index : indexNames) {
                DeleteIndexRequest deleteMapping = Requests.deleteIndexRequest(index);
                ElasticFactory.getClient().admin().indices().delete(deleteMapping).actionGet();
            }
        }
    }

    /**
     * 根据别名移除所有索引的别名
     */
    public static void removeAllAliasByAlias(String indexAlias) {
        if (StringUtils.isNotBlank(indexAlias)) {
            String[] indexNames = getIndexesByAlias(indexAlias);
            for (Object index : indexNames) {
                removeAlias(index.toString(), indexAlias);
            }
        }
    }

    /**
     * 根据索引的名字删除所有索引
     *
     * @param indexNames
     */
    public static void removeAllIndexByIndex(String... indexNames) {
        if (indexNames != null) {
            for (String index : indexNames) {
                DeleteIndexRequest deleteMapping = Requests.deleteIndexRequest(index);
                ElasticFactory.getClient().admin().indices().delete(deleteMapping).actionGet();
            }
        }
    }

    /**
     * 删除所有没有别名的索引,用于清理,也就是说,所有有用的索引都需要有别名,或者不要使用此方法
     */
    public static void removeAllIndexNoAlias() {
        Set<String> needRemoveIndex = getAllNoAliasIndexes();
        String[] clearIndexes = needRemoveIndex.toArray(new String[needRemoveIndex.size()]);
        removeAllIndexByIndex(clearIndexes);
        log.warn("已经清理了所有没有别名的索引:" + Arrays.toString(clearIndexes));
    }

    private static Set<String> getAllNoAliasIndexes() {
        GetIndexResponse getIndexResponse = GetIndexAction.INSTANCE.newRequestBuilder(ElasticFactory.getClient()).get();
        String[] indices = getIndexResponse.getIndices();
        ImmutableOpenMap<String, List<AliasMetaData>> indexAliasMap = getIndexResponse.getAliases();
        Set<String> needRemoveIndex = new HashSet<>();
        for (String index : indices) {
            if (indexAliasMap.get(index) == null) {
                needRemoveIndex.add(index);
            }
        }
        return needRemoveIndex;
    }

    /**
     * 添加别名
     *
     * @param indexName  索引名
     * @param indexAlias 索引别名
     */
    public static IndicesAliasesResponse addAlias(String indexName, String indexAlias) {
        IndicesAliasesRequestBuilder indicesAliasesRequestBuilder = IndicesAliasesAction.INSTANCE.newRequestBuilder(ElasticFactory.getClient());
        return indicesAliasesRequestBuilder.addAlias(indexName, indexAlias).get();
    }

    /**
     * 删除别名
     *
     * @param indexName  索引名
     * @param indexAlias 索引别名
     */
    public static IndicesAliasesResponse removeAlias(String indexName, String indexAlias) {
        IndicesAliasesRequestBuilder indicesAliasesRequestBuilder = IndicesAliasesAction.INSTANCE.newRequestBuilder(ElasticFactory.getClient());
        return indicesAliasesRequestBuilder.removeAlias(indexName, indexAlias).get();
    }

    /**
     * 别名重定向
     *
     * @param indexNameOld 旧的索引名
     * @param indexNameNew 新的索引名
     * @param indexAlias   索引别名
     */
    public static void renameAlias(String indexNameOld, String indexNameNew, String indexAlias) {
        IndicesAliasesRequestBuilder indicesAliasesRequestBuilder = IndicesAliasesAction.INSTANCE.newRequestBuilder(ElasticFactory.getClient());
        indicesAliasesRequestBuilder.removeAlias(indexNameOld, indexAlias).addAlias(indexNameNew, indexAlias).get();
    }

    /**
     * 批量别名重定向
     */
    public static void renameAliases(Iterable<RenameAlias> renameAliases) {
        if (!renameAliases.iterator().hasNext()) return;
        //----第一次使用时,由于存在旧有索引,所以需要删除旧有索引,仅处理没有别名的索引,有别名的索引都不处理,
        Set<String> needRemoveIndex = getAllNoAliasIndexes();
        needRemoveIndex.remove("matcher");//特例,不管它
        for (RenameAlias renameAlias : renameAliases) {
            if (needRemoveIndex.contains(renameAlias.getIndexAlias()) && StringUtils.isNotBlank(renameAlias.getIndexAlias())) {
                try {
                    String randomName = SecurityUtil.MD5("random" + (9999999 * Math.random()));
                    ElasticAliasQuery.addAlias(renameAlias.getIndexAlias(), randomName);
                    ElasticAliasQuery.removeAllIndexByIndex(randomName);
                } catch (IndexNotFoundException e1) {
                    //一个个删除,因为可能就是这个原因出错,所以不能批量删除
                }
            }
        }
        //----
        IndicesAliasesRequestBuilder indicesAliasesRequestBuilder = IndicesAliasesAction.INSTANCE.newRequestBuilder(ElasticFactory.getClient());
        for (RenameAlias renameAlias : renameAliases) {
            if (StringUtils.isNotBlank(renameAlias.getIndexNameOld())) {
                indicesAliasesRequestBuilder.removeAlias(renameAlias.getIndexNameOld(), renameAlias.getIndexAlias());
            }
            indicesAliasesRequestBuilder.addAlias(renameAlias.getIndexNameNew(), renameAlias.getIndexAlias());
        }
        indicesAliasesRequestBuilder.get();
    }

    @Data
    public static class RenameAlias {
        private String indexNameOld;
        private String indexNameNew;
        private String indexAlias;

        private RenameAlias() {
        }

        @java.beans.ConstructorProperties({"indexNameOld", "indexNameNew", "indexAlias"})
        public RenameAlias(String indexNameOld, String indexNameNew, String indexAlias) {
            this.indexNameOld = indexNameOld;
            this.indexNameNew = indexNameNew;
            this.indexAlias = indexAlias;
        }

        public static List<RenameAlias> create(String[] indexNameOlds, IndexNameType indexNameType) {
            if (!indexNameType.needRenameAlias()) return Collections.emptyList();
            List<RenameAlias> renameAliasList = new LinkedList<>();
            if (indexNameOlds.length > 0) {
                for (String old : indexNameOlds) {
                    renameAliasList.add(new RenameAlias(old, indexNameType.getIndexRealName(), indexNameType.getIndexAlias()));
                }
            } else {
                renameAliasList.add(new RenameAlias("", indexNameType.getIndexRealName(), indexNameType.getIndexAlias()));
            }
            return renameAliasList;
        }
    }
}
