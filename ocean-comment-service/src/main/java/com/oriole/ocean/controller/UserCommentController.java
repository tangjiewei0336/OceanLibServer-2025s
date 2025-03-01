package com.oriole.ocean.controller;

import com.oriole.ocean.common.enumerate.*;
import com.oriole.ocean.common.auth.AuthUser;
import com.oriole.ocean.common.po.mongo.UserBehaviorEntity;
import com.oriole.ocean.common.po.mongo.comment.*;
import com.oriole.ocean.common.po.mysql.NotifyEntity;
import com.oriole.ocean.common.service.NotifyService;
import com.oriole.ocean.common.service.NotifySubscriptionService;
import com.oriole.ocean.common.service.UserBehaviorService;
import com.oriole.ocean.common.vo.AuthUserEntity;
import com.oriole.ocean.common.vo.BusinessException;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.CommentServiceImpl;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.oriole.ocean.common.enumerate.ResultCode.SUCCESS;
import static com.oriole.ocean.common.enumerate.ResultCode.UNAUTHORIZED_OPERATION;

@RequestMapping("/comment")
@RestController
public class UserCommentController {

    @Autowired
    CommentServiceImpl commentService;
    @DubboReference
    UserBehaviorService userBehaviorService;
    @DubboReference
    NotifyService notifyService;
    @DubboReference
    NotifySubscriptionService notifySubscriptionService;

    @ApiOperation(value = "删除评论区评论或回复")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bindID", value = "资源唯一编号", required = true),
            @ApiImplicitParam(name = "mainType", value = "主资源类型（文档/纸条）", required = true),
            @ApiImplicitParam(name = "commentID", value = "评论唯一编号", required = true)
    })
    @RequestMapping(value = "/deleteComment", method = RequestMethod.GET)
    public MsgEntity<String> deleteComment(@AuthUser AuthUserEntity authUser,
                                           @RequestParam Integer bindID,
                                           @RequestParam MainType mainType,
                                           @RequestParam(required = false) String commentID,
                                           @RequestParam(required = false) String deleteReason
    ) {
        //判断用户是否有权限删除评论，并确定删除类型
        CommentStatusType commentStatusType = null;
        if (deleteReason != null){
            if(authUser.isAdmin()){
                commentStatusType = CommentStatusType.ILLEGAL_CLOSURE;
                // TODO: 记录管理员删除行为
            } else {
                throw new BusinessException(UNAUTHORIZED_OPERATION);
            }
        }
        if (commentStatusType == null) {
            if (commentID.contains("_")) {//判别是回复还是评论
                CommentReplyEntity commentReplyEntity = commentService.getCommentReplyByCommentReplyID(bindID, mainType, commentID);
                if (authUser.isUserOwn(commentReplyEntity.getReplyBuildUsername())) { //自己删除自己的评论
                    commentStatusType = CommentStatusType.USER_DELETE;
                }
            } else {
                CommentEntity commentEntity = commentService.getCommentByCommentID(bindID, mainType, commentID);
                if (authUser.isUserOwn(commentEntity.getCommentBuildUsername())) { //自己删除自己的回复
                    commentStatusType = CommentStatusType.USER_DELETE;
                }
            }
        }
        if (commentStatusType == null) { // 不是自己的评论或回复
            if(commentService.isCanDeleteComment(bindID, authUser.getUsername(), mainType)){  // 判断是否有权删除
                commentStatusType = CommentStatusType.USER_CLOSURE;
            }
        }
        if (commentStatusType == null) { // 仍然没有找到合乎清理的删除理由，说明没有权限
            throw new BusinessException("-2", "权限不足，必须为评论发布者或文档发布者");
        }
        //执行删除
        commentService.changeCommentOrReplyStatus(bindID, mainType, commentID, commentStatusType);
        if (!commentID.contains("_")) {
            // 只有评论才计入评论量统计，因此回复删除行为不可扣除计数
            commentService.commentStatisticsChange(bindID, "-1", mainType);
        }
        return new MsgEntity<>(SUCCESS);
    }

    @RequestMapping(value = "/addComment", method = RequestMethod.POST)
    public MsgEntity<AbstractComment> addComment(@AuthUser AuthUserEntity authUser,
                                                 @RequestParam Integer bindID,
                                                 @RequestParam MainType mainType,
                                                 @RequestParam(required = false, defaultValue = "false") Boolean isReply,
                                                 @RequestParam String commentContent,
                                                 @RequestParam(required = false) String replyInCommentID,
                                                 @RequestParam(required = false) String replyToCommentReplyID,
                                                 @RequestParam(required = false) String replyToCommentReplier
    ) {
        //检查是否有评论权限
        if(!commentService.isCanAddComment(bindID, authUser.getUsername(), mainType) && !authUser.isAdmin()){
            throw new BusinessException("-2", "发布者已经关闭评论区");
        }
        //添加用户评论
        String cid = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
        AbstractComment returnEntity;
        //构建用户消息事件
        NotifyEntity notifyEntity = new NotifyEntity(NotifyType.REMIND,authUser.getUsername());
        notifyEntity.setTargetIDAndType(String.valueOf(bindID),mainType);
        notifyEntity.setContent(commentContent);
        if (!isReply) {
            CommentEntity commentEntity = new CommentEntity(cid, authUser.getUsername(), commentContent);
            commentService.addComment(bindID, mainType, commentEntity);
            commentEntity.setBuildDate(handleTime(commentEntity.getBuildDate()));
            returnEntity = commentEntity;
            notifyEntity.setAction(NotifyAction.NEW_COMMENT);// 新评论事件不面向任何其他评论

        } else {
            CommentReplyEntity fileCommentReplyEntity = new CommentReplyEntity(
                    replyInCommentID + "_" + cid, authUser.getUsername(),
                    replyToCommentReplier,
                    replyToCommentReplyID,
                    commentContent);
            commentService.addCommentReply(bindID, mainType, replyInCommentID, fileCommentReplyEntity);
            fileCommentReplyEntity.setBuildDate(handleTime(fileCommentReplyEntity.getBuildDate()));
            returnEntity = fileCommentReplyEntity;
            notifyEntity.setAction(NotifyAction.NEW_REPLY);
            if(!replyToCommentReplyID.isEmpty()){
                notifyEntity.setCommentID(replyToCommentReplyID);
            }else {
                notifyEntity.setCommentID(replyInCommentID);
            }
        }
        // 增加用户消息订阅事件：用户需要订阅自己发布的评论或回复的动态
        List<NotifyAction> notifyActionList = new ArrayList<>();
        notifyActionList.add(NotifyAction.LIKE_COMMENT);
        notifyActionList.add(NotifyAction.NEW_REPLY);
        notifySubscriptionService.setNotifySubscription(authUser.getUsername(), notifyActionList,
                returnEntity.getId(), NotifySubscriptionTargetType.COMMENT);

        // 产生用户消息事件
        notifyService.addNotify(notifyEntity);

        if (!isReply) {
            // 只有评论才计入文章的评论量统计
            commentService.commentStatisticsChange(bindID, "+1", mainType);
            //TODO:通知
        }

        return new MsgEntity<>("SUCCESS", "1", returnEntity);
    }

    @RequestMapping(value = "/getComment", method = RequestMethod.GET)
    public MsgEntity<CommentsListEntity> getComment(@RequestParam Integer bindID,
                                                    @RequestParam MainType mainType,
                                                    @RequestParam Integer commentCount, @RequestParam Integer replyCount,
                                                    @RequestParam Integer pageNum) {
        CommentsListEntity commentsListEntity = commentService.getCommentsListEntity(bindID, mainType);
        if (commentsListEntity == null) {
            // 新建评论文档
            return new MsgEntity<>("SUCCESS", "1", commentService.initCommentArea(bindID, mainType));
        }
        List<CommentEntity> commentEntities = commentService.getComments(bindID, mainType, commentCount, replyCount, pageNum);
        //处理评论
        for (CommentEntity comment : commentEntities) {
            //处理评论日期信息
            comment.setBuildDate(handleTime(comment.getBuildDate()));
            //处理回复对应日期信息
            for (CommentReplyEntity reply : comment.getReplyCommentList()) {
                reply.setBuildDate(handleTime(reply.getBuildDate()));
            }
        }
        commentsListEntity.setComments(commentEntities);
        return new MsgEntity<>("SUCCESS", "1", commentsListEntity);
    }

    @RequestMapping(value = "/getCommentReply", method = RequestMethod.GET)
    public MsgEntity<CommentEntity> getCommentReply(
            @RequestParam Integer bindID,
            @RequestParam MainType mainType,
            @RequestParam String commentID, @RequestParam Integer replyCount,
            @RequestParam Integer pageNum) {
        CommentEntity commentEntity = commentService.getCommentByCommentID(bindID, mainType, commentID);
        ArrayList<CommentReplyEntity> commentReplyList = commentService.getCommentAllReplies(bindID, mainType, commentID, replyCount, pageNum);
        for (CommentReplyEntity reply : commentReplyList) {
            reply.setBuildDate(handleTime(reply.getBuildDate()));
        }
        commentEntity.setReplyCommentList(commentReplyList);
        commentEntity.setBuildDate(handleTime(commentEntity.getBuildDate()));
        return new MsgEntity<>("SUCCESS", "1", commentEntity);
    }

    @ApiOperation(value = "评价一条评论或回复", notes = "isLike参数为评价操作的分类（true表示为点赞操作，false表示为点踩操作），isCancel为是否为取消评价操作。如isLike为false，isCancel为true，则表示取消点踩操作")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bindID", value = "唯一资源编号", required = true),
            @ApiImplicitParam(name = "mainType", value = "资源类型", required = true),
            @ApiImplicitParam(name = "commentID", value = "评论唯一编号", required = true),
            @ApiImplicitParam(name = "isCancel", value = "是否为取消操作", required = true),
            @ApiImplicitParam(name = "isLike", value = "是否为点赞操作", required = true)
    })
    @RequestMapping(value = "/evaluateComment", method = RequestMethod.GET)
    public MsgEntity<String> evaluateComment(@AuthUser AuthUserEntity authUser,
                                             @RequestParam Integer bindID,
                                             @RequestParam MainType mainType,
                                             @RequestParam String commentID,
                                             @RequestParam Boolean isCancel,
                                             @RequestParam Boolean isLike) {
        // 添加用户行为
        UserBehaviorEntity userBehaviorEntity = new UserBehaviorEntity(bindID, mainType, authUser.getUsername(), null);
        List<EvaluateType> evaluates = userBehaviorService.checkAndGetUserEvaluateBehavior(userBehaviorEntity, isCancel, isLike);
        userBehaviorService.setUserEvaluateBehavior(userBehaviorEntity, evaluates);

        // 修改数据库
        commentService.evaluateComment(bindID, mainType, commentID, evaluates);
        return new MsgEntity<>(SUCCESS);
    }


    public String handleTime(String buildDate) {
        long timeDifference = new Date().getTime() - Long.parseLong(buildDate);
        long days = timeDifference / 60 / 60 / 1000 / 24;
        if (days < 1) {
            long mins = (timeDifference % (1000 * 60 * 60 * 24)) / (1000 * 60);
            if (mins == 0) {
                return "刚刚";
            } else if (mins <= 60) {
                return mins + "分钟前";
            } else {
                return (mins / 60) + "小时前";
            }
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return simpleDateFormat.format(new Date(Long.parseLong(buildDate)));
        }
    }
}