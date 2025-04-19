package com.oriole.ocean.controller;

import com.github.pagehelper.PageHelper;
import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.sun.istack.internal.NotNull;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("/question")
public class QuestionController {


    /**
     * POST /new : 获得一个新的问题ID
     * 获得一个新的问题ID，可以可选地为之添加内容，否则默认为空。
     *
     * @param title   (optional)
     * @param content (optional)
     * @return 提问成功，返回操作提示信息 (status code 200)
     * or 未授权 (status code 401)
     * or 服务器内部错误 (status code 500)
     */
    @ApiOperation(value = "获得一个新的问题ID", nickname = "newPost", notes = "获得一个新的问题ID，可以可选地为之添加内容，否则默认为空。", response = MsgEntity.class, tags = {"用户服务器/ocean-qa-service/QuestionController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "提问成功，返回操作提示信息", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @RequestMapping(value = "/new",
            produces = {"application/json"},
            consumes = {"multipart/form-data"},
            method = RequestMethod.POST)
    public ResponseEntity<MsgEntity<Long>> newPost(@AuthUser AuthUserEntity authUser,
                                                   @ApiParam(value = "") @RequestPart(value = "title", required = false) String title,
                                                   @ApiParam(value = "") @RequestPart(value = "content", required = false) String content) {


        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    /**
     * GET /list : 获取所有问题列表
     * 分页获取问题列表。
     *
     * @param page     页码 (required)
     * @param pageSize 每页显示的问题数量 (required)
     * @return 返回问题列表 (status code 200)
     * or 未授权 (status code 401)
     * or 服务器内部错误 (status code 500)
     */
    @ApiOperation(value = "获取所有问题列表", nickname = "listGet", notes = "分页获取问题列表。", response = MsgEntity.class, tags = {"用户服务器/ocean-qa-service/QuestionController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "返回问题列表", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @RequestMapping(value = "/list",
            produces = {"application/json"},
            method = RequestMethod.GET)
    public ResponseEntity<MsgEntity<PageHelper>> listGet(@AuthUser AuthUserEntity authUser,
                                                         @NotNull @ApiParam(value = "页码", required = true) @Valid @RequestParam(value = "page", required = true) Integer page, @NotNull @ApiParam(value = "每页显示的问题数量", required = true) @Valid @RequestParam(value = "pageSize", required = true) Integer pageSize) {


        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }


    /**
     * PUT /update : 修改/发布/隐藏问题
     * 修改指定问题的标题和/或内容，返回更新后的问题信息。 如果有图片，视频或者音频，需要先调用docFileService/uploadFile进行上传。
     *
     * @param questionId 要修改的问题 ID (required)
     * @param isPost     是否转换为发布状态，isPost和isHide中最多只能有一个为True (optional)
     * @param isHide     是否转换为草稿状态，isPost和isHide中最多只能有一个为True (optional)
     * @param setReward  (optional)
     * @param title      (optional)
     * @param content    (optional)
     * @return 问题修改成功，返回更新后的问题信息 (status code 200)
     * or 未授权 (status code 401)
     * or 服务器内部错误 (status code 500)
     */
    @ApiOperation(value = "修改/发布/隐藏问题", nickname = "updatePut", notes = "修改指定问题的标题和/或内容，返回更新后的问题信息。 如果有图片，视频或者音频，需要先调用docFileService/uploadFile进行上传。", response = MsgEntity.class, tags = {"用户服务器/ocean-qa-service/QuestionController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "问题修改成功，返回更新后的问题信息", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @RequestMapping(value = "/update",
            produces = {"application/json"},
            consumes = {"multipart/form-data"},
            method = RequestMethod.PUT)
    public ResponseEntity<MsgEntity<String>> updatePut(@AuthUser AuthUserEntity authUser,
                                                       @NotNull @ApiParam(value = "要修改的问题 ID", required = true) @Valid @RequestParam(value = "questionId", required = true) String questionId, @ApiParam(value = "是否转换为发布状态，isPost和isHide中最多只能有一个为True") @Valid @RequestParam(value = "isPost", required = false) Integer isPost, @ApiParam(value = "是否转换为草稿状态，isPost和isHide中最多只能有一个为True") @Valid @RequestParam(value = "isHide", required = false) Integer isHide, @ApiParam(value = "") @Valid @RequestParam(value = "setReward", required = false) Integer setReward, @ApiParam(value = "") @RequestPart(value = "title", required = false) String title, @ApiParam(value = "") @RequestPart(value = "content", required = false) String content) {

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

    /**
     * DELETE /delete : 删除问题
     * 删除指定问题，自动鉴权，返回操作提示信息。 删除对于草稿和已发布的都有效。 删除是软删除。
     *
     * @param questionId 要删除的问题 ID (required)
     * @return 问题删除成功，返回操作提示信息 (status code 200)
     * or 未授权 (status code 401)
     * or 服务器内部错误 (status code 500)
     */
    @ApiOperation(value = "删除问题", nickname = "deleteDelete", notes = "删除指定问题，自动鉴权，返回操作提示信息。 删除对于草稿和已发布的都有效。 删除是软删除。", response = MsgEntity.class, tags = {"用户服务器/ocean-qa-service/QuestionController",})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "问题删除成功，返回操作提示信息", response = MsgEntity.class),
            @ApiResponse(code = 401, message = "未授权", response = Object.class),
            @ApiResponse(code = 500, message = "服务器内部错误", response = Object.class)})
    @RequestMapping(value = "/delete",
            produces = {"application/json"},
            consumes = {"multipart/form-data"},
            method = RequestMethod.DELETE)
    public ResponseEntity<MsgEntity<String>> deleteDelete(@AuthUser AuthUserEntity authUser,
                                                          @NotNull @ApiParam(value = "要删除的问题 ID", required = true) @Valid @RequestParam(value = "questionId", required = true) String questionId) {


        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


}

