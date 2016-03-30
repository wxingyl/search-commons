package com.tqmall.search.util;

import org.elasticsearch.action.*;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.exists.ExistsRequest;
import org.elasticsearch.action.exists.ExistsRequestBuilder;
import org.elasticsearch.action.exists.ExistsResponse;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.explain.ExplainRequestBuilder;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.fieldstats.FieldStatsRequest;
import org.elasticsearch.action.fieldstats.FieldStatsRequestBuilder;
import org.elasticsearch.action.fieldstats.FieldStatsResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.indexedscripts.delete.DeleteIndexedScriptRequest;
import org.elasticsearch.action.indexedscripts.delete.DeleteIndexedScriptRequestBuilder;
import org.elasticsearch.action.indexedscripts.delete.DeleteIndexedScriptResponse;
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptRequest;
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptRequestBuilder;
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptResponse;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptRequest;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptRequestBuilder;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptResponse;
import org.elasticsearch.action.percolate.*;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.suggest.SuggestRequest;
import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.action.termvectors.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.support.Headers;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by 刘一波 on 16/3/2.
 * E-Mail:yibo.liu@tqmall.com
 */
public class Client implements org.elasticsearch.client.Client {
    private org.elasticsearch.client.Client client;

    public Client(org.elasticsearch.client.Client client) {
        this.client = client;
    }

    @Override
    public AdminClient admin() {
        return client.admin();
    }

    @Override
    public ActionFuture<IndexResponse> index(IndexRequest request) {
        return client.index(request);
    }

    @Override
    public void index(IndexRequest request, ActionListener<IndexResponse> listener) {
        client.index(request, listener);
    }

    @Override
    public IndexRequestBuilder prepareIndex() {
        return client.prepareIndex();
    }

    @Override
    public ActionFuture<UpdateResponse> update(UpdateRequest request) {
        return client.update(request);
    }

    @Override
    public void update(UpdateRequest request, ActionListener<UpdateResponse> listener) {
        client.update(request, listener);
    }

    @Override
    public UpdateRequestBuilder prepareUpdate() {
        return client.prepareUpdate();
    }

    @Override
    @Deprecated
    public UpdateRequestBuilder prepareUpdate(String index, String type, String id) {
        return client.prepareUpdate(index, type, id);
    }

    @Override
    @Deprecated
    public IndexRequestBuilder prepareIndex(String index, String type) {
        return client.prepareIndex(index, type);
    }

    @Override
    @Deprecated
    public IndexRequestBuilder prepareIndex(String index, String type, @Nullable String id) {
        return client.prepareIndex(index, type, id);
    }

    @Override
    public ActionFuture<DeleteResponse> delete(DeleteRequest request) {
        return client.delete(request);
    }

    @Override
    public void delete(DeleteRequest request, ActionListener<DeleteResponse> listener) {
        client.delete(request, listener);
    }

    @Override
    public DeleteRequestBuilder prepareDelete() {
        return client.prepareDelete();
    }

    @Override
    @Deprecated
    public DeleteRequestBuilder prepareDelete(String index, String type, String id) {
        return client.prepareDelete(index, type, id);
    }

    @Override
    public ActionFuture<BulkResponse> bulk(BulkRequest request) {
        return client.bulk(request);
    }

    @Override
    public void bulk(BulkRequest request, ActionListener<BulkResponse> listener) {
        client.bulk(request, listener);
    }

    @Override
    public BulkRequestBuilder prepareBulk() {
        return client.prepareBulk();
    }

    @Override
    public ActionFuture<GetResponse> get(GetRequest request) {
        return client.get(request);
    }

    @Override
    public void get(GetRequest request, ActionListener<GetResponse> listener) {
        client.get(request, listener);
    }

    @Override
    public GetRequestBuilder prepareGet() {
        return client.prepareGet();
    }

    @Override
    @Deprecated
    public GetRequestBuilder prepareGet(String index, @Nullable String type, String id) {
        return client.prepareGet(index, type, id);
    }

    @Override
    public PutIndexedScriptRequestBuilder preparePutIndexedScript() {
        return client.preparePutIndexedScript();
    }

    @Override
    public PutIndexedScriptRequestBuilder preparePutIndexedScript(@Nullable String scriptLang, String id, String source) {
        return client.preparePutIndexedScript(scriptLang, id, source);
    }

    @Override
    public void deleteIndexedScript(DeleteIndexedScriptRequest request, ActionListener<DeleteIndexedScriptResponse> listener) {
        client.deleteIndexedScript(request, listener);
    }

    @Override
    public ActionFuture<DeleteIndexedScriptResponse> deleteIndexedScript(DeleteIndexedScriptRequest request) {
        return client.deleteIndexedScript(request);
    }

    @Override
    public DeleteIndexedScriptRequestBuilder prepareDeleteIndexedScript() {
        return client.prepareDeleteIndexedScript();
    }

    @Override
    public DeleteIndexedScriptRequestBuilder prepareDeleteIndexedScript(@Nullable String scriptLang, String id) {
        return client.prepareDeleteIndexedScript(scriptLang, id);
    }

    @Override
    public void putIndexedScript(PutIndexedScriptRequest request, ActionListener<PutIndexedScriptResponse> listener) {
        client.putIndexedScript(request, listener);
    }

    @Override
    public ActionFuture<PutIndexedScriptResponse> putIndexedScript(PutIndexedScriptRequest request) {
        return client.putIndexedScript(request);
    }

    @Override
    public GetIndexedScriptRequestBuilder prepareGetIndexedScript() {
        return client.prepareGetIndexedScript();
    }

    @Override
    public GetIndexedScriptRequestBuilder prepareGetIndexedScript(@Nullable String scriptLang, String id) {
        return client.prepareGetIndexedScript(scriptLang, id);
    }

    @Override
    public void getIndexedScript(GetIndexedScriptRequest request, ActionListener<GetIndexedScriptResponse> listener) {
        client.getIndexedScript(request, listener);
    }

    @Override
    public ActionFuture<GetIndexedScriptResponse> getIndexedScript(GetIndexedScriptRequest request) {
        return client.getIndexedScript(request);
    }

    @Override
    public ActionFuture<MultiGetResponse> multiGet(MultiGetRequest request) {
        return client.multiGet(request);
    }

    @Override
    public void multiGet(MultiGetRequest request, ActionListener<MultiGetResponse> listener) {
        client.multiGet(request, listener);
    }

    @Override
    public MultiGetRequestBuilder prepareMultiGet() {
        return client.prepareMultiGet();
    }

    @Override
    @Deprecated
    public ActionFuture<CountResponse> count(CountRequest request) {
        return client.count(request);
    }

    @Override
    @Deprecated
    public void count(CountRequest request, ActionListener<CountResponse> listener) {
        client.count(request, listener);
    }

    @Override
    @Deprecated
    public CountRequestBuilder prepareCount(String... indices) {
        return client.prepareCount(indices);
    }

    @Override
    @Deprecated
    public ActionFuture<ExistsResponse> exists(ExistsRequest request) {
        return client.exists(request);
    }

    @Override
    @Deprecated
    public void exists(ExistsRequest request, ActionListener<ExistsResponse> listener) {
        client.exists(request, listener);
    }

    @Override
    @Deprecated
    public ExistsRequestBuilder prepareExists(String... indices) {
        return client.prepareExists(indices);
    }

    @Override
    public ActionFuture<SuggestResponse> suggest(SuggestRequest request) {
        return client.suggest(request);
    }

    @Override
    public void suggest(SuggestRequest request, ActionListener<SuggestResponse> listener) {
        client.suggest(request, listener);
    }

    @Override
    public SuggestRequestBuilder prepareSuggest(String... indices) {
        return client.prepareSuggest(indices);
    }

    @Override
    public ActionFuture<SearchResponse> search(SearchRequest request) {
        return client.search(request);
    }

    @Override
    public void search(SearchRequest request, ActionListener<SearchResponse> listener) {
        client.search(request, listener);
    }

    /**
     * 请用{@link #prepareSearch(IndexNameType...)}代替
     *
     * @param indices
     * @return
     */
    @Override
    @Deprecated
    public SearchRequestBuilder prepareSearch(String... indices) {
        return client.prepareSearch(indices);
    }

    @Override
    public ActionFuture<SearchResponse> searchScroll(SearchScrollRequest request) {
        return client.searchScroll(request);
    }

    @Override
    public void searchScroll(SearchScrollRequest request, ActionListener<SearchResponse> listener) {
        client.searchScroll(request, listener);
    }

    @Override
    public SearchScrollRequestBuilder prepareSearchScroll(String scrollId) {
        return client.prepareSearchScroll(scrollId);
    }

    @Override
    public ActionFuture<MultiSearchResponse> multiSearch(MultiSearchRequest request) {
        return client.multiSearch(request);
    }

    @Override
    public void multiSearch(MultiSearchRequest request, ActionListener<MultiSearchResponse> listener) {
        client.multiSearch(request, listener);
    }

    @Override
    public MultiSearchRequestBuilder prepareMultiSearch() {
        return client.prepareMultiSearch();
    }

    @Override
    public ActionFuture<TermVectorsResponse> termVectors(TermVectorsRequest request) {
        return client.termVectors(request);
    }

    @Override
    public void termVectors(TermVectorsRequest request, ActionListener<TermVectorsResponse> listener) {
        client.termVectors(request, listener);
    }

    @Override
    public TermVectorsRequestBuilder prepareTermVectors() {
        return client.prepareTermVectors();
    }

    @Override
    @Deprecated
    public TermVectorsRequestBuilder prepareTermVectors(String index, String type, String id) {
        return client.prepareTermVectors(index, type, id);
    }

    @Override
    @Deprecated
    public ActionFuture<TermVectorsResponse> termVector(TermVectorsRequest request) {
        return client.termVector(request);
    }

    @Override
    @Deprecated
    public void termVector(TermVectorsRequest request, ActionListener<TermVectorsResponse> listener) {
        client.termVector(request, listener);
    }

    @Override
    @Deprecated
    public TermVectorsRequestBuilder prepareTermVector() {
        return client.prepareTermVector();
    }

    @Override
    @Deprecated
    public TermVectorsRequestBuilder prepareTermVector(String index, String type, String id) {
        return client.prepareTermVector(index, type, id);
    }

    @Override
    public ActionFuture<MultiTermVectorsResponse> multiTermVectors(MultiTermVectorsRequest request) {
        return client.multiTermVectors(request);
    }

    @Override
    public void multiTermVectors(MultiTermVectorsRequest request, ActionListener<MultiTermVectorsResponse> listener) {
        client.multiTermVectors(request, listener);
    }

    @Override
    public MultiTermVectorsRequestBuilder prepareMultiTermVectors() {
        return client.prepareMultiTermVectors();
    }

    @Override
    public ActionFuture<PercolateResponse> percolate(PercolateRequest request) {
        return client.percolate(request);
    }

    @Override
    public void percolate(PercolateRequest request, ActionListener<PercolateResponse> listener) {
        client.percolate(request, listener);
    }

    @Override
    public PercolateRequestBuilder preparePercolate() {
        return client.preparePercolate();
    }

    @Override
    public ActionFuture<MultiPercolateResponse> multiPercolate(MultiPercolateRequest request) {
        return client.multiPercolate(request);
    }

    @Override
    public void multiPercolate(MultiPercolateRequest request, ActionListener<MultiPercolateResponse> listener) {
        client.multiPercolate(request, listener);
    }

    @Override
    public MultiPercolateRequestBuilder prepareMultiPercolate() {
        return client.prepareMultiPercolate();
    }

    @Override
    @Deprecated
    public ExplainRequestBuilder prepareExplain(String index, String type, String id) {
        return client.prepareExplain(index, type, id);
    }

    @Override
    public ActionFuture<ExplainResponse> explain(ExplainRequest request) {
        return client.explain(request);
    }

    @Override
    public void explain(ExplainRequest request, ActionListener<ExplainResponse> listener) {
        client.explain(request, listener);
    }

    @Override
    public ClearScrollRequestBuilder prepareClearScroll() {
        return client.prepareClearScroll();
    }

    @Override
    public ActionFuture<ClearScrollResponse> clearScroll(ClearScrollRequest request) {
        return client.clearScroll(request);
    }

    @Override
    public void clearScroll(ClearScrollRequest request, ActionListener<ClearScrollResponse> listener) {
        client.clearScroll(request, listener);
    }

    @Override
    public FieldStatsRequestBuilder prepareFieldStats() {
        return client.prepareFieldStats();
    }

    @Override
    public ActionFuture<FieldStatsResponse> fieldStats(FieldStatsRequest request) {
        return client.fieldStats(request);
    }

    @Override
    public void fieldStats(FieldStatsRequest request, ActionListener<FieldStatsResponse> listener) {
        client.fieldStats(request, listener);
    }

    @Override
    public Settings settings() {
        return client.settings();
    }

    @Override
    public Headers headers() {
        return client.headers();
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> ActionFuture<Response> execute(Action<Request, Response, RequestBuilder> action, Request request) {
        return client.execute(action, request);
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> void execute(Action<Request, Response, RequestBuilder> action, Request request, ActionListener<Response> listener) {
        client.execute(action, request, listener);
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> RequestBuilder prepareExecute(Action<Request, Response, RequestBuilder> action) {
        return client.prepareExecute(action);
    }

    @Override
    public ThreadPool threadPool() {
        return client.threadPool();
    }

    @Override
    public void close() {
        client.close();
    }

    public UpdateRequestBuilder prepareUpdate(IndexNameType indexNameType, String string) {
        return prepareUpdate(indexNameType.getIndexName(), indexNameType.getIndexType(), string);
    }

    public IndexRequestBuilder prepareIndex(IndexNameType indexNameType) {
        return client.prepareIndex(indexNameType.getIndexName(), indexNameType.getIndexType());
    }

    public IndexRequestBuilder prepareIndex(IndexNameType indexNameType, @Nullable String id) {
        return client.prepareIndex(indexNameType.getIndexName(), indexNameType.getIndexType(), id);
    }

    public GetRequestBuilder prepareGet(IndexNameType indexNameType, String id) {
        return client.prepareGet(indexNameType.getIndexName(), indexNameType.getIndexType(), id);
    }

    public ExplainRequestBuilder prepareExplain(IndexNameType indexNameType, String id) {
        return client.prepareExplain(indexNameType.getIndexName(), indexNameType.getIndexType(), id);
    }

    public TermVectorsRequestBuilder prepareTermVector(IndexNameType indexNameType, String id) {
        return client.prepareTermVector(indexNameType.getIndexName(), indexNameType.getIndexType(), id);
    }


    public TermVectorsRequestBuilder prepareTermVectors(IndexNameType indexNameType, String id) {
        return client.prepareTermVectors(indexNameType.getIndexName(), indexNameType.getIndexType(), id);
    }

    public DeleteRequestBuilder prepareDelete(IndexNameType indexNameType, String id) {
        return client.prepareDelete(indexNameType.getIndexName(), indexNameType.getIndexType(), id);
    }

    public SearchRequestBuilder prepareSearch(IndexNameType... indexNameTypes) {
        Set<String> index = new HashSet<>();
        Set<String> type = new HashSet<>();
        for (IndexNameType indexNameType : indexNameTypes) {
            index.add(indexNameType.getIndexName());
            type.add(indexNameType.getIndexType());
        }
        return client.prepareSearch(index.toArray(new String[index.size()])).setTypes(type.toArray(new String[type.size()]));
    }

}
