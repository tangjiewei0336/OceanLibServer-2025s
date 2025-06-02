package com.oriole.ocean.service.impl;

import com.oriole.ocean.common.po.es.QuestionEntity;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Service
public class QaESearchServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(QaESearchServiceImpl.class);

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private RestHighLevelClient client;

    public SearchHits<QuestionEntity> searchQuestions(String keywords, Integer page, Integer rows) {
        // 构建查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.should(QueryBuilders.matchQuery("title", keywords).fuzziness(Fuzziness.AUTO));
        boolQuery.should(QueryBuilders.matchQuery("content", keywords).fuzziness(Fuzziness.AUTO));
        boolQuery.should(QueryBuilders.matchQuery("answer", keywords).fuzziness(Fuzziness.AUTO));

        // 构建高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title").field("content").field("answer");
        highlightBuilder.preTags("<em>").postTags("</em>");

        // 构建查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
            .withQuery(boolQuery)
            .withHighlightBuilder(highlightBuilder);

        // 分页
        if (page != null && rows != null) {
            queryBuilder.withPageable(org.springframework.data.domain.PageRequest.of(page - 1, rows));
        }

        // 执行搜索
        return elasticsearchRestTemplate.search(queryBuilder.build(), QuestionEntity.class);
    }

    public ArrayList<String> suggestTitle(String keyword, Integer rows) {
        try {
            // 创建搜索请求
            SearchRequest searchRequest = new SearchRequest("question_entity");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            
            // 创建完成建议器
            CompletionSuggestionBuilder suggestionBuilder = SuggestBuilders
                .completionSuggestion("title.suggest")  // 使用正确的字段路径
                .prefix(keyword)
                .size(rows != null ? rows : 10);
            
            // 添加建议器到搜索请求
            SuggestBuilder suggestBuilder = new SuggestBuilder();
            suggestBuilder.addSuggestion("title_suggest", suggestionBuilder);
            searchSourceBuilder.suggest(suggestBuilder);
            
            // 执行搜索
            searchRequest.source(searchSourceBuilder);
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            
            // 处理结果
            Set<String> returnSet = new HashSet<>();
            Suggest suggest = response.getSuggest();
            if (suggest != null) {
                CompletionSuggestion completionSuggestion = suggest.getSuggestion("title_suggest");
                for (CompletionSuggestion.Entry entry : completionSuggestion.getEntries()) {
                    for (CompletionSuggestion.Entry.Option option : entry.getOptions()) {
                        returnSet.add(option.getText().string());
                    }
                }
            }
            
            return new ArrayList<>(returnSet);
        } catch (Exception e) {
            log.error("Error getting title suggestions", e);
            return new ArrayList<>();
        }
    }
}
