package com.oriole.ocean.controller;

import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.enumerate.BehaviorType;
import com.oriole.ocean.common.enumerate.MainType;
import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.po.mongo.AnswerEntity;
import com.oriole.ocean.common.po.mongo.UserBehaviorEntity;
import com.oriole.ocean.common.service.UserBehaviorService;
import com.oriole.ocean.common.service.UserWalletService;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.QuestionService;
import com.oriole.ocean.service.AnswerService;
import com.oriole.ocean.service.impl.QaESearchServiceImpl;
import com.sun.istack.internal.NotNull;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/qaService/question")
public class QuestionController {

    private final QuestionService questionService;

    @DubboReference
    UserBehaviorService userBehaviorService;

    @DubboReference
    UserWalletService userWalletService;

    @Autowired
    QaESearchServiceImpl eSearchService;

    @Autowired
    AnswerService answerService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @ApiOperation(value = "获得一个新的问题ID", nickname = "newPost", notes = "获得一个新的问题ID，可以可选地为之添加内容，否则默认为空。", response = MsgEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "提问成功，返回操作提示信息", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @PostMapping(value = "/new", produces = {"application/json"})
    public ResponseEntity<MsgEntity<Integer>> newPost(
            @AuthUser AuthUserEntity authUser,
            @ApiParam(value = "") @RequestParam(value = "title", required = false) String title,
            @ApiParam(value = "") @RequestParam(value = "content", required = false) String content) {

        MsgEntity<Integer> result = questionService.createQuestion(title, content, authUser.getUsername());
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "获取所有问题列表", nickname = "listGet", notes = "分页获取问题列表。", response = MsgEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "返回问题列表", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @GetMapping(value = "/list", produces = {"application/json"})
    public ResponseEntity<MsgEntity<Page<QuestionEntity>>> listGet(
            @AuthUser AuthUserEntity authUser,
            @NotNull @ApiParam(value = "查询用户或所有", required = false) @Valid @RequestParam(value = "username", required = false) String username,
            @NotNull @ApiParam(value = "排序方式，0:时间更新 1:热度", required = false) @Valid @RequestParam(value = "sort", required = false) Integer sort,
            @NotNull @ApiParam(value = "页码", required = true) @Valid @RequestParam(value = "page", required = true) Integer page,
            @NotNull @ApiParam(value = "每页显示的问题数量", required = true) @Valid @RequestParam(value = "pageSize", required = true) Integer pageSize) {
        MsgEntity<Page<QuestionEntity>> result = null;
        if(sort == null) {
            sort = 0;
        }

        if (result == null) {
            return ResponseEntity.badRequest().body(new MsgEntity<>("ERROR", "Failed to retrieve questions", null));
        }
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "修改/发布/隐藏问题", nickname = "updatePut", notes = "修改指定问题的标题和/或内容，返回更新后的问题信息。", response = MsgEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "问题修改成功，返回更新后的问题信息", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @PutMapping(value = "/update", produces = {"application/json"})
    public ResponseEntity<MsgEntity<QuestionEntity>> updatePut(
            @AuthUser AuthUserEntity authUser,
            @NotNull @ApiParam(value = "要修改的问题 ID", required = true) @Valid @RequestParam(value = "questionId", required = true) Integer questionId,
            @ApiParam(value = "是否转换为发布状态，isPost和isHide中最多只能有一个为True") @Valid @RequestParam(value = "isPost", required = false) Boolean isPost,
            @ApiParam(value = "是否转换为草稿状态，isPost和isHide中最多只能有一个为True") @Valid @RequestParam(value = "isHide", required = false) Boolean isHide,
            @ApiParam(value = "") @Valid @RequestParam(value = "setReward", required = false) Integer setReward,
            @ApiParam(value = "") @RequestPart(value = "title", required = false) String title,
            @ApiParam(value = "") @RequestPart(value = "content", required = false) String content) {

        MsgEntity<QuestionEntity> result = questionService.updateQuestion(
                questionId, title, content, isPost, isHide, setReward, authUser.getUsername());
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "删除问题", nickname = "deleteDelete", notes = "删除指定问题，自动鉴权，返回操作提示信息。", response = MsgEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "问题删除成功，返回操作提示信息", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @DeleteMapping(value = "/delete", produces = {"application/json"})
    public ResponseEntity<MsgEntity<String>> deleteDelete(
            @AuthUser AuthUserEntity authUser,
            @NotNull @ApiParam(value = "要删除的问题 ID", required = true) @Valid @RequestParam(value = "questionId", required = true) Integer questionId) {

        MsgEntity<String> result = questionService.deleteQuestion(questionId, authUser.getUsername());
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "获取最近浏览的问题", nickname = "recentlyViewedGet", notes = "获取最近浏览的问题列表。", response = MsgEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "返回最近浏览的问题列表", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @GetMapping(value = "/recentlyViewed", produces = {"application/json"})
    public ResponseEntity<MsgEntity<List<QuestionEntity>>> getRecentlyViewedQuestions(@AuthUser AuthUserEntity authUser) {
        String username = authUser.getUsername();
        UserBehaviorEntity UserBehaviorEntityQuery = new UserBehaviorEntity(null, MainType.QUESTION, username, BehaviorType.DO_READ);
        List<UserBehaviorEntity> userBehaviorEntities = userBehaviorService.findAllBehaviorRecords(UserBehaviorEntityQuery);

        List<Integer> fileIDs = userBehaviorEntities.stream().map(UserBehaviorEntity::getBindID).distinct().collect(Collectors.toList());

        List<QuestionEntity> questions = new ArrayList<>();
        if (fileIDs.size() > 0) {
            questions = questionService.getQuestionByIds(fileIDs);
        }
        MsgEntity<List<QuestionEntity>> result = new MsgEntity<>("SUCCESS", "1", questions);
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "获取问题详情", nickname = "getQuestionDetails", notes = "获取指定问题的详细信息。", response = MsgEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "返回问题详情", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @GetMapping(value = "/details", produces = {"application/json"})
    public ResponseEntity<MsgEntity<QuestionEntity>> getQuestionDetails(
            @AuthUser AuthUserEntity authUser,
            @NotNull @ApiParam(value = "问题的 ID", required = true) @Valid @RequestParam(value = "questionId", required = true) Integer questionId) {

        QuestionEntity question = questionService.getQuestionById(questionId);

        // 记录浏览数据
        userBehaviorService.setBehaviorRecord(new UserBehaviorEntity(questionId, MainType.QUESTION, authUser.getUsername(), BehaviorType.DO_READ));

        if (question == null) {
            return ResponseEntity.status(404).body(new MsgEntity<>("ERROR", "Question not found", null));
        }
        MsgEntity<QuestionEntity> result = new MsgEntity<>("SUCCESS", "1", question);
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "搜索问题", nickname = "searchQuestions", notes = "根据关键词搜索问题。", response = MsgEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "返回搜索结果", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @GetMapping(value = "/search", produces = {"application/json"})
    public MsgEntity<SearchHits<com.oriole.ocean.common.po.es.QuestionEntity>> searchQuestions(
            @AuthUser AuthUserEntity authUser,
            @NotNull @ApiParam(value = "搜索关键词", required = true) @Valid @RequestParam(value = "keywords", required = true) String keyword,
            @NotNull @ApiParam(value = "页码", required = true) @Valid @RequestParam(value = "page", required = true) Integer page,
            @NotNull @ApiParam(value = "每页显示的问题数量", required = true) @Valid @RequestParam(value = "rows", required = true) Integer pageSize) {

        SearchHits<com.oriole.ocean.common.po.es.QuestionEntity> searchHits = eSearchService.searchQuestions(keyword, page, pageSize);
        return new MsgEntity<>("SUCCESS", "1", searchHits);
    }

    @ApiOperation(value = "获取问题标题建议", nickname = "suggestTitle", notes = "根据关键词获取问题标题建议。", response = MsgEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "返回标题建议", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @GetMapping(value = "/suggestTitle", produces = {"application/json"})
    public MsgEntity<ArrayList<String>> suggestTitle(@RequestParam String keyword, @RequestParam Integer rows) {
        ArrayList<String> suggests = eSearchService.suggestTitle(keyword, rows);
        return new MsgEntity<>("SUCCESS", "1", suggests);
    }
}