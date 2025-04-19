package com.oriole.ocean.controller;

import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.po.mongo.AnswerEntity;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.sun.istack.internal.NotNull;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("/answer")
public class AnswerController {
    /**
     * POST /submit : 提交一个回答
     * 提交回答，返回操作提示信息。
     *
     * @param questionId 回答所属的问题 ID (required)
     * @param answer     (optional)
     * @return 回答提交成功，返回操作提示信息 (status code 200)
     * or 未授权 (status code 401)
     * or 服务器内部错误 (status code 500)
     */
    @ApiOperation(value = "提交一个回答", nickname = "submitPost", notes = "提交回答，返回操作提示信息。", response = MsgEntity.class, tags = {"用户服务器/ocean-qa-service/AnswerController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "回答提交成功，返回操作提示信息", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @RequestMapping(value = "/submit",
            produces = {"application/json"},
            consumes = {"multipart/form-data"},
            method = RequestMethod.POST)
    public ResponseEntity<MsgEntity> submitAnswer(@AuthUser AuthUserEntity authUser, @NotNull @ApiParam(value = "回答所属的问题 ID", required = true) @Valid @RequestParam(value = "questionId", required = true) String questionId, @ApiParam(value = "") @RequestPart(value = "answer", required = false) String answer) {

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * GET /list : 获取指定问题的所有回答
     * 分页获取某个问题的回答列表。
     *
     * @param questionId 问题的 ID (required)
     * @param page       页码 (required)
     * @param pageSize   每页显示的回答数量 (required)
     * @return 返回回答列表 (status code 200)
     * or 未授权 (status code 401)
     * or 服务器内部错误 (status code 500)
     */
    @ApiOperation(value = "获取指定问题的所有回答", nickname = "listGet", notes = "分页获取某个问题的回答列表。", response = MsgEntity.class, tags = {"用户服务器/ocean-qa-service/AnswerController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "返回回答列表", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @RequestMapping(value = "/list",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity<MsgEntity<List<AnswerEntity>>> listGetAnswer(@AuthUser AuthUserEntity authUser, @NotNull @ApiParam(value = "问题的 ID", required = true) @Valid @RequestParam(value = "questionId", required = true) String questionId, @NotNull @ApiParam(value = "页码", required = true) @Valid @RequestParam(value = "page", required = true) Integer page, @NotNull @ApiParam(value = "每页显示的回答数量", required = true) @Valid @RequestParam(value = "pageSize", required = true) Integer pageSize) {

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * PUT /update : 修改回答
     * 修改指定回答的内容（需提供用户 ID 验证权限），返回更新后的回答信息。
     *
     * @param answerId 要修改的回答 ID (required)
     * @param content  修改后的回答内容 (required)
     * @return 回答修改成功，返回更新后的回答信息 (status code 200)
     * or 未授权 (status code 401)
     * or 服务器内部错误 (status code 500)
     */
    @ApiOperation(value = "修改回答", nickname = "updatePut", notes = "修改指定回答的内容（需提供用户 ID 验证权限），返回更新后的回答信息。", response = MsgEntity.class, tags = {"用户服务器/ocean-qa-service/AnswerController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "回答修改成功，返回更新后的回答信息", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @RequestMapping(value = "/update",
            produces = {"application/json"},
            method = RequestMethod.PUT)
    public ResponseEntity<MsgEntity<String>> updateAnswer(@AuthUser AuthUserEntity authUser, @NotNull @ApiParam(value = "要修改的回答 ID", required = true) @Valid @RequestParam(value = "answerId", required = true) String answerId, @NotNull @ApiParam(value = "修改后的回答内容", required = true) @Valid @RequestParam(value = "content", required = true) String content) {


        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * DELETE /delete : 删除回答
     * 删除指定回答，返回操作提示信息。 删除是软删除。
     *
     * @param answerId 要删除的回答 ID (required)
     * @return 回答删除成功，返回操作提示信息 (status code 200)
     * or 未授权 (status code 401)
     * or 服务器内部错误 (status code 500)
     */
    @ApiOperation(value = "删除回答", nickname = "deleteDelete", notes = "删除指定回答，返回操作提示信息。 删除是软删除。", response = Object.class, tags = {"用户服务器/ocean-qa-service/AnswerController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "回答删除成功，返回操作提示信息", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @RequestMapping(value = "/delete",
            produces = {"application/json"},
            method = RequestMethod.DELETE)
    public ResponseEntity<MsgEntity<String>> deleteAnswer(@AuthUser AuthUserEntity authUser,
                                                          @NotNull @ApiParam(value = "要删除的回答 ID", required = true) @Valid @RequestParam(value = "answerId", required = true) String answerId) {

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
