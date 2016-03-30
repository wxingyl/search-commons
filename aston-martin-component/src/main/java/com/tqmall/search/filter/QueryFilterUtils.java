package com.tqmall.search.filter;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;

/**
 * 构建query或者filter的工具类
 * <p/>
 * Created by 刘一波 on 15/12/10.
 * E-Mail:yibo.liu@tqmall.com
 */
public final class QueryFilterUtils {
    public final static BoolQueryBuilder EMPTY_BOOL_QUERY = QueryBuilders.boolQuery();

    /**
     * 多个字段，一个值，‘或’搜索
     *
     * @return
     */
    public static QueryBuilder orFilterByMultiFields(String[] value, String... fields) {
        BoolQueryBuilder filterBuilder = QueryBuilders.boolQuery();

        if (value.length == 1) {
            for (String field : fields)
                filterBuilder.should(QueryBuilders.prefixQuery(field, value[0]));
        } else if (value.length > 1) {
            for (String field : fields)
                filterBuilder.should(QueryBuilders.termsQuery(field, value));
        }

        return filterBuilder;
    }

    /**
     * 此方法为了避免在filterBuilder中添加过多空的过滤器
     *
     * @param field            查询字段
     * @param valueList        查询值，支持不等于及null查询
     * @param andFilterBuilder 在查询中使用的andFilterBuilder，当valueList参数为空，或者其它内容而导致其无意义时，
     *                         andFilterBuilder不变，否则，把参数生成的filter加入其中
     */
    public static void termsFilter(String field, List<String> valueList, BoolQueryBuilder andFilterBuilder) {
        QueryBuilder filterBuilder = termsFilter(field, valueList);
        if (filterBuilder != EMPTY_BOOL_QUERY) {
            andFilterBuilder.must(filterBuilder);
        }
    }

    /**
     * 查询，返回filter，可支持null和普通值，可使用!前缀表示不等于
     *
     * @param field     查询字段
     * @param valueList 查询值，支持不等于及null查询
     * @return FilterBuilder
     */
    public static QueryBuilder termsFilter(String field, List<String> valueList) {
        QueryBuilder filterBuilderEqual;
        QueryBuilder filterBuilderNotEqual;
        if (valueList != null && valueList.size() > 0 && valueList.get(0) != null) {
            // 是/非的搜索，这里添加。以"!"开头的都是非搜索
            Boolean filterNull = null;//null 则不处理|true为字段为空|false为字段不为空
            List<String> notFilter = new ArrayList<>();
            List<String> andFilter = new ArrayList<>();
            for (String str : valueList) {
                if (str.startsWith("!") || str.startsWith("^")) {//兼容老版本，也支持^
                    str = str.substring(1);
                    if (str.equalsIgnoreCase("null")) filterNull = filterNull != null;
                    else notFilter.add(str);
                } else {
                    if (str.equalsIgnoreCase("null")) filterNull = true;
                    else andFilter.add(str);
                }
            }

            //此处逻辑，只有等于和不等于，存在等于，则不等于即不生效，否则，不等于生效。
            //等于
            BoolQueryBuilder orFilterBuilder = QueryBuilders.boolQuery();
            if (andFilter.size() > 0 || (filterNull != null && filterNull)) {//==value||==null
                if (andFilter.size() > 0) {
                    filterBuilderEqual = QueryBuilders.termsQuery(field, andFilter);//==value
                    orFilterBuilder.should(filterBuilderEqual);
                }
                if (filterNull != null && filterNull) {
                    orFilterBuilder.should(QueryBuilders.missingQuery(field));//==null
                }
                return orFilterBuilder;//有等于的情况下，只处理等于，等于包含等于null值
            }

            //不等于
            BoolQueryBuilder andFilterBuilder = QueryBuilders.boolQuery();
            if (notFilter.size() > 0 || filterNull != null) {//!=value||!=null
                if (notFilter.size() > 0) {
                    filterBuilderNotEqual = QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery(field, notFilter));//!=value
                    andFilterBuilder.must(filterBuilderNotEqual);
                }
                if (filterNull != null) {
                    andFilterBuilder.must(QueryBuilders.existsQuery(field));//!=null
                }
                return andFilterBuilder;
            }

        }
        return EMPTY_BOOL_QUERY;
    }

    /**
     * 根据官方文档，当没有使用聚类或者聚类的过滤条件和查询条件相同时，使用查询，而不使用后置过滤器。<br />
     * 仅在聚类的过滤条件和查询不同时，才考虑使用后置过滤器。<br />
     * 此方法用于包装查询和后置过滤器，后置过滤器不影响文档打分。<br />
     *
     * @param queryBuilder  查询
     * @param filterBuilder 原来放在post_filter中的后置过滤器
     * @return BoolQueryBuilder
     */
    public static BoolQueryBuilder wrapQueryAndPostFilter(QueryBuilder queryBuilder, QueryBuilder filterBuilder) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (queryBuilder != null) {
            boolQueryBuilder.must(queryBuilder);
        }
        if (filterBuilder != null) {
            boolQueryBuilder.must(QueryBuilders.constantScoreQuery(filterBuilder).boost(0));
        }
        return boolQueryBuilder;
    }

}
