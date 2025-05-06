package com.oriole.ocean.controller;

import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.po.mongo.QuestionEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.QuestionService;
import com.sun.istack.internal.NotNull;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/question")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @ApiOperation(value = "获得一个新的问题ID", nickname = "newPost", notes = "获得一个新的问题ID，可以可选地为之添加内容，否则默认为空。", response = MsgEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "提问成功，返回操作提示信息", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @PostMapping(value = "/new", produces = {"application/json"}, consumes = {"multipart/form-data"})
    public ResponseEntity<MsgEntity<String>> newPost(
            @AuthUser AuthUserEntity authUser,
            @ApiParam(value = "") @RequestPart(value = "title", required = false) String title,
            @ApiParam(value = "") @RequestPart(value = "content", required = false) String content) {

        MsgEntity<String> result = questionService.createQuestion(title, content, authUser.getUsername());
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
            @NotNull @ApiParam(value = "页码", required = true) @Valid @RequestParam(value = "page", required = true) Integer page,
            @NotNull @ApiParam(value = "每页显示的问题数量", required = true) @Valid @RequestParam(value = "pageSize", required = true) Integer pageSize) {

        MsgEntity<Page<QuestionEntity>> result = questionService.getQuestions(page, pageSize);
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "修改/发布/隐藏问题", nickname = "updatePut", notes = "修改指定问题的标题和/或内容，返回更新后的问题信息。", response = MsgEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "问题修改成功，返回更新后的问题信息", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @PutMapping(value = "/update", produces = {"application/json"}, consumes = {"multipart/form-data"})
    public ResponseEntity<MsgEntity<QuestionEntity>> updatePut(
            @AuthUser AuthUserEntity authUser,
            @NotNull @ApiParam(value = "要修改的问题 ID", required = true) @Valid @RequestParam(value = "questionId", required = true) String questionId,
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
    @DeleteMapping(value = "/delete", produces = {"application/json"}, consumes = {"multipart/form-data"})
    public ResponseEntity<MsgEntity<String>> deleteDelete(
            @AuthUser AuthUserEntity authUser,
            @NotNull @ApiParam(value = "要删除的问题 ID", required = true) @Valid @RequestParam(value = "questionId", required = true) String questionId) {

        MsgEntity<String> result = questionService.deleteQuestion(questionId, authUser.getUsername());
        return ResponseEntity.ok(result);
    }
}