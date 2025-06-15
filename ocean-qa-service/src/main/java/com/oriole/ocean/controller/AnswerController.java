package com.oriole.ocean.controller;

import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.enumerate.MainType;
import com.oriole.ocean.common.po.mongo.AnswerEntity;
import com.oriole.ocean.common.po.mysql.UserCollectionEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.AnswerService;
import com.oriole.ocean.service.UserCollectionService;
import com.sun.istack.internal.NotNull;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.Valid;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/qaService/answer")
public class AnswerController {

    private final AnswerService answerService;

    @DubboReference
    UserCollectionService userCollectionService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @ApiOperation(value = "提交一个回答", nickname = "submitPost", notes = "提交回答，返回操作提示信息。", response = MsgEntity.class, tags = {"用户服务器/ocean-qa-service/AnswerController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "回答提交成功，返回操作提示信息", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @PostMapping(value = "/submit", produces = {"application/json"}, consumes = {"multipart/form-data"})
    public ResponseEntity<MsgEntity<Integer>> submitAnswer(
            @AuthUser AuthUserEntity authUser,
            @NotNull @ApiParam(value = "回答所属的问题 ID", required = true) @Valid @RequestParam(value = "questionId", required = true) Integer questionId,
            @ApiParam(value = "") @RequestPart(value = "answer", required = false) String answer) {

        MsgEntity<Integer> result = answerService.submitAnswer(questionId, answer, authUser.getUsername());
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "获取指定问题的所有回答", nickname = "listGet", notes = "分页获取某个问题的回答列表。", response = MsgEntity.class, tags = {"用户服务器/ocean-qa-service/AnswerController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "返回回答列表", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @GetMapping(value = "/list", produces = {"application/json"})
    public ResponseEntity<MsgEntity<Page<AnswerEntity>>> listGetAnswer(
            @AuthUser AuthUserEntity authUser,
            @NotNull @ApiParam(value = "问题的 ID", required = true) @Valid @RequestParam(value = "questionId", required = true) Integer questionId,
            @NotNull @ApiParam(value = "页码", required = true) @Valid @RequestParam(value = "page", required = true) Integer page,
            @NotNull @ApiParam(value = "每页显示的回答数量", required = true) @Valid @RequestParam(value = "pageSize", required = true) Integer pageSize,
            @NotNull @ApiParam(value = "管理员可选择展示已删除的", required = false) @Valid @RequestParam(value = "includeDeleted", required = false) Boolean includeDeleted) {

        if(includeDeleted == null)
            includeDeleted = false;

        if(includeDeleted.equals(Boolean.TRUE) && !(authUser.isAdmin() || authUser.isSuperAdmin())){
            return ResponseEntity.badRequest().body(new MsgEntity<>("ERROR", "Only admin can view deleted questions.", null));
        };

        MsgEntity<Page<AnswerEntity>> result = answerService.getAnswersByQuestionId(questionId, page, pageSize, authUser.getUsername(), includeDeleted);
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "修改回答", nickname = "updatePut", notes = "修改指定回答的内容（需提供用户 ID 验证权限），返回更新后的回答信息。", response = MsgEntity.class, tags = {"用户服务器/ocean-qa-service/AnswerController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "回答修改成功，返回更新后的回答信息", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @PutMapping(value = "/update", produces = {"application/json"})
    public ResponseEntity<MsgEntity<AnswerEntity>> updateAnswer(
            @AuthUser AuthUserEntity authUser,
            @NotNull @ApiParam(value = "要修改的回答 ID", required = true) @Valid @RequestParam(value = "answerId", required = true) Integer answerId,
            @NotNull @ApiParam(value = "修改后的回答内容", required = true) @Valid @RequestParam(value = "content", required = true) String content) {


        MsgEntity<AnswerEntity> result = answerService.updateAnswer(answerId, content, authUser.getUsername(), authUser.isAdmin() || authUser.isSuperAdmin());
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "删除回答", nickname = "deleteDelete", notes = "删除指定回答，返回操作提示信息。 删除是软删除。", response = Object.class, tags = {"用户服务器/ocean-qa-service/AnswerController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "回答删除成功，返回操作提示信息", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @DeleteMapping(value = "/delete", produces = {"application/json"})
    public ResponseEntity<MsgEntity<String>> deleteAnswer(
            @AuthUser AuthUserEntity authUser,
            @NotNull @ApiParam(value = "要删除的回答 ID", required = true) @Valid @RequestParam(value = "answerId", required = true) Integer answerId) {

        AnswerEntity answer = answerService.getAnswerById(answerId, null);
        if (!answer.getUserId().equals(authUser.getUsername()) && !(authUser.isAdmin() || authUser.isSuperAdmin())) {
            // 如果不是管理员或超级管理员，并且不是问题的创建者，则禁止修改
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this answer");
        }

        MsgEntity<String> result = answerService.deleteAnswer(answerId);
        return ResponseEntity.ok(result);
    }

    // 我最近的回答
    @ApiOperation(value = "获取某个人最近的回答", nickname = "getMyAnswers", notes = "获取我最近的回答", response = MsgEntity.class, tags = {"用户服务器/ocean-qa-service/AnswerController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "返回我最近的回答", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @GetMapping(value = "/myAnswers", produces = {"application/json"})
    public ResponseEntity<MsgEntity<Page<AnswerEntity>>> getMyAnswers(
            @AuthUser AuthUserEntity authUser,
            @NotNull @ApiParam(value = "页码", required = true) @Valid @RequestParam(value = "page", required = true) Integer page,
            @NotNull @ApiParam(value = "每页显示的回答数量", required = true) @Valid @RequestParam(value = "pageSize", required = true) Integer pageSize,
            @ApiParam(value = "用户名", required = false) @Valid @RequestParam(value = "username", required = false) String username,
            @NotNull @ApiParam(value = "管理员可选择展示已删除的", required = false) @Valid @RequestParam(value = "includeDeleted", required = false) Boolean includeDeleted) {

        if (username == null) {
            username = authUser.getUsername();
        }

        if (includeDeleted == null){
            includeDeleted = false;
        }

        MsgEntity<Page<AnswerEntity>> result = answerService.getAnswersByUserId(username, page, pageSize, (boolean)includeDeleted);
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "获取所有回答", nickname = "getAllAnswers", notes = "分页获取所有回答列表", response = MsgEntity.class, tags = {"用户服务器/ocean-qa-service/AnswerController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "返回回答列表", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @GetMapping(value = "/all", produces = {"application/json"})
    public ResponseEntity<MsgEntity<Page<AnswerEntity>>> getAllAnswers(
            @AuthUser AuthUserEntity authUser,
            @NotNull @ApiParam(value = "页码", required = true) @Valid @RequestParam(value = "page", required = true) Integer page,
            @NotNull @ApiParam(value = "每页显示的回答数量", required = true) @Valid @RequestParam(value = "pageSize", required = true) Integer pageSize,
            @NotNull @ApiParam(value = "管理员可选择展示已删除的", required = false) @Valid @RequestParam(value = "includeDeleted", required = false) Boolean includeDeleted) {


        if(includeDeleted == null)
            includeDeleted = false;
        if(includeDeleted.equals(Boolean.TRUE) && !(authUser.isAdmin() || authUser.isSuperAdmin())){
            return ResponseEntity.badRequest().body(new MsgEntity<>("ERROR", "Only admin can view deleted answers.", null));
        };

        MsgEntity<Page<AnswerEntity>> result = answerService.getAllAnswers(page, pageSize, authUser.getUsername(), includeDeleted);
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "我收藏的回答查询", nickname = "getAnswersByIds", response = MsgEntity.class, tags = {"用户服务器/ocean-qa-service/AnswerController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "返回回答列表", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @GetMapping(value = "/likedAnswers", produces = {"application/json"})
    public ResponseEntity<MsgEntity<Page<AnswerEntity>>> getLikedAnswers(
            @AuthUser AuthUserEntity authUser,
            @RequestParam(required = false) String username,
            @NotNull @ApiParam(value = "页码", required = true) @RequestParam(value = "page", required = true) Integer page,
            @NotNull @ApiParam(value = "每页显示的回答数量", required = true) @RequestParam(value = "pageSize", required = true) Integer pageSize,
            @NotNull @ApiParam(value = "收藏夹 ID", required = true) @RequestParam(value = "collectionID", required = true) String collectionID) {

        username = authUser.getAllowOperationUsername(username);

        UserCollectionEntity.CollectionEntity collectionEntity = userCollectionService.getCollectionByUsernameAndCollectionID(username, collectionID, MainType.ANSWER);

        Integer[] answerIds = collectionEntity.getItems().toArray(new Integer[0]);

        MsgEntity<Page<AnswerEntity>> result = answerService.getAnswersByIds(answerIds, page, pageSize, authUser.getUsername());
        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "/batch", nickname = "batchGetAnswers", notes = "批量获取回答信息", response = MsgEntity.class, tags = {"用户服务器/ocean-qa-service/AnswerController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "返回回答列表", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @PostMapping(value = "/batch", produces = {"application/json"})
    public ResponseEntity<MsgEntity<Page<AnswerEntity>>> batchGetAnswers(
            @AuthUser AuthUserEntity authUser,
            @NotNull @ApiParam(value = "回答 ID 列表", required = true) @Valid @RequestBody( required = true) HashMap<String, Integer[]> itemList) {

        Integer[] answerIds = itemList.get("itemList");
        MsgEntity<Page<AnswerEntity>> result = answerService.getAnswersByIds(answerIds);
        return ResponseEntity.ok(result);
    }


}