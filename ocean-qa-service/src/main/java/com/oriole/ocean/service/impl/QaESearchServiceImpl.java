package com.oriole.ocean.service.impl;

import com.oriole.ocean.common.po.es.FileSearchEntity;
import com.oriole.ocean.common.po.mongo.QuestionEntity;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class QaESearchServiceImpl {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    public SearchHits<QuestionEntity> searchQuestions(String keywords, Integer page, Integer rows) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .should(QueryBuilders.fuzzyQuery("analyzer_title", keywords).fuzziness(Fuzziness.AUTO))
                .should(QueryBuilders.fuzzyQuery("analyzer_content", keywords).fuzziness(Fuzziness.AUTO))
                .should(QueryBuilders.fuzzyQuery("analyzer_abstract_content", keywords).fuzziness(Fuzziness.AUTO))
                .must(QueryBuilders.multiMatchQuery(keywords,"analyzer_title","analyzer_content","analyzer_abstract_content"))
                .must(QueryBuilders.matchQuery("is_approved", "true")); //必须是已经被核准的才能被检索出来


        //构建高亮查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withHighlightFields(
                        new HighlightBuilder.Field("analyzer_title"),
                        new HighlightBuilder.Field("analyzer_abstract_content"),
                        new HighlightBuilder.Field("analyzer_content"))
                .withHighlightBuilder(new HighlightBuilder().preTags("<span class='highlight'>").postTags("</span>"))
                .withPageable(PageRequest.of(page - 1, rows)).build();

        SearchHits<QuestionEntity> searchHits = elasticsearchRestTemplate.search(searchQuery, QuestionEntity.class);
        return searchHits;
    }
    public ArrayList<String> suggestTitle(String keyword,Integer rows) {
        return suggest("suggest_title",keyword,rows);
    }

    public ArrayList<String> suggest(String fieldName, String keyword,Integer rows) {
        HashSet<String> returnSet = new LinkedHashSet<>(); // 用于存储查询到的结果
        // 创建CompletionSuggestionBuilder
        CompletionSuggestionBuilder textBuilder = SuggestBuilders.completionSuggestion(fieldName) // 指定字段名
                .size(rows) // 设定返回数量
                .skipDuplicates(true); // 去重

        // 创建suggestBuilder并将completionBuilder添加进去
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("suggest_text", textBuilder)
                .setGlobalText(keyword);
        // 执行请求
        Suggest suggest = elasticsearchRestTemplate.suggest(suggestBuilder, elasticsearchRestTemplate.getIndexCoordinatesFor(FileSearchEntity.class)).getSuggest();
        // 取出结果
        Suggest.Suggestion<Suggest.Suggestion.Entry<CompletionSuggestion.Entry.Option>> textSuggestion = suggest.getSuggestion("suggest_text");
        for (Suggest.Suggestion.Entry<CompletionSuggestion.Entry.Option> entry : textSuggestion.getEntries()) {
            List<CompletionSuggestion.Entry.Option> options = entry.getOptions();
            for (Suggest.Suggestion.Entry.Option option : options) {
                returnSet.add(option.getText().toString());
            }
        }
        return new ArrayList<>(returnSet);
    }

}
