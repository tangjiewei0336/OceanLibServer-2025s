package com.oriole.ocean.service;

import com.alibaba.fastjson.JSONObject;
import com.oriole.ocean.common.enumerate.DocStatisticItemType;
import com.oriole.ocean.common.enumerate.EvaluateType;
import com.oriole.ocean.common.enumerate.MainType;
import com.oriole.ocean.common.po.mongo.comment.CommentEntity;
import com.oriole.ocean.common.po.mongo.comment.CommentReplyEntity;
import com.oriole.ocean.common.po.mongo.comment.CommentStatusType;
import com.oriole.ocean.common.po.mongo.comment.CommentsListEntity;
import com.oriole.ocean.common.po.mysql.FileEntity;
import com.oriole.ocean.common.service.FileExtraService;
import com.oriole.ocean.common.service.FileService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl {

    @Autowired
    private MongoTemplate mongoTemplate;

    @DubboReference
    private FileExtraService fileExtraService;

    @DubboReference
    private FileService fileService ;

    public void commentStatisticsChange(Integer itemID, String addNumber, MainType mainType) {
        switch (mainType){
            case DOCUMENT:
                fileExtraService.docBaseStatisticsInfoChange(itemID, addNumber, DocStatisticItemType.COMMENT);
                break;
            case NOTE:
                break;
        }
    }

    public boolean isCanDeleteComment(Integer bindID, String username, MainType mainType) {
        switch (mainType) {
            case DOCUMENT:// 文档类的作者可以删除自己文档下面的评论
                FileEntity fileEntity = fileService.getFileBaseInfoByFileID(bindID);
                return fileEntity.getUploadUsername().equals(username); //作者有权删除其他的人的评论
            case NOTE:
                return false;
                //TODO:未完成NOTE的策略
            default:
                return false;
        }
    }

    public boolean isCanAddComment(Integer bindID, String username, MainType mainType) {
        switch (mainType) {
            case DOCUMENT:// 文档类的作者可以删除自己文档下面的评论
                FileEntity fileEntity = fileService.getFileBaseInfoByFileID(bindID);
                return !fileEntity.getIsAllowComment().equals((byte) 0);
            case NOTE:
                return false;
            //TODO:未完成NOTE的策略
            default:
                return false;
        }
    }

    private String getCollectionName(MainType mainType){
        return "comments_" + mainType.toString();
    }

    public CommentsListEntity getCommentsListEntity(Integer bindID, MainType mainType) {
        Query query = new Query();
        query.fields().exclude("comments");
        query.addCriteria(Criteria.where("bindID").is(bindID));
        CommentsListEntity commentsListEntity = mongoTemplate.findOne(query, CommentsListEntity.class, getCollectionName(mainType));
        return commentsListEntity;
    }

    //分页获取指定分类和绑定ID下的全部评论(包括回复，回复不分页，只能取前N条)
    public List<CommentEntity> getComments(Integer bindID, MainType mainType, Integer commentCount, Integer queryReplyCount, Integer pageNum) {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("bindID").is(bindID)));
        operations.add(Aggregation.unwind("comments"));
        operations.add(Aggregation.sort(Sort.by(new Sort.Order(Sort.Direction.DESC, "comments.hotValue")))
                .and(Sort.by(new Sort.Order(Sort.Direction.DESC, "comments.likeNumber")))
                .and(Sort.by(new Sort.Order(Sort.Direction.ASC, "comments.dislikeNumber"))));
        ProjectionOperation projectionOperation =
                Aggregation.project("commentBuildUsername", "commentContent", "hotValue", "likeNumber", "dislikeNumber", "buildDate", "replyCount")
                        .andExpression("comments._id").as("_id")
                        .andExpression("comments.commentBuildUsername").as("commentBuildUsername")
                        .andExpression("comments.commentContent").as("commentContent")
                        .andExpression("comments.hotValue").as("hotValue")
                        .andExpression("comments.commentStatus").as("commentStatus")
                        .andExpression("comments.likeNumber").as("likeNumber")
                        .andExpression("comments.dislikeNumber").as("dislikeNumber")
                        .andExpression("comments.buildDate").as("buildDate")
                        .andExpression("comments.replyCount").as("replyCount");
        if (queryReplyCount != 0) {
            operations.add(projectionOperation.andExpression("comments.replyCommentList").slice(queryReplyCount, 0).as("replyCommentList"));
        } else {
            operations.add(projectionOperation);
        }
        operations.add(Aggregation.skip(commentCount * (pageNum - 1)));
        operations.add(Aggregation.limit(commentCount));
        Aggregation aggregation = Aggregation.newAggregation(operations);

        // 查询结果
        AggregationResults<CommentEntity> results = mongoTemplate.aggregate(aggregation, getCollectionName(mainType), CommentEntity.class);
        return results.getMappedResults();
    }

    //分页获取指定分类和绑定ID下的指定评论的全部回复
    public ArrayList<CommentReplyEntity> getCommentAllReplies(Integer bindID, MainType mainType, String commentID, Integer replyCount, Integer pageNum) {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("bindID").is(bindID)));
        operations.add(Aggregation.unwind("comments"));
        operations.add(Aggregation.match(Criteria.where("comments._id").is(commentID)));

        operations.add(Aggregation.project("replyCommentList")
                .andExpression("comments.replyCommentList").slice(replyCount, replyCount * (pageNum - 1)).as("replyCommentList")
        );
        Aggregation aggregation = Aggregation.newAggregation(operations);

        // 查询结果
        AggregationResults<CommentEntity> results = mongoTemplate.aggregate(aggregation, getCollectionName(mainType), CommentEntity.class);
        List<CommentEntity> result = results.getMappedResults();
        return result.get(0).getReplyCommentList();
    }

    //获取指定分类和绑定ID下的指定评论详细信息，不含回复
    public CommentEntity getCommentByCommentID(Integer bindID, MainType mainType, String commentID) {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("bindID").is(bindID)));
        operations.add(Aggregation.unwind("comments"));
        operations.add(Aggregation.match(Criteria.where("comments._id").is(commentID)));

        operations.add(Aggregation.project("commentBuildUsername", "commentContent", "hotValue", "likeNumber", "dislikeNumber", "replyCount", "buildDate")
                .andExpression("comments._id").as("_id")
                .andExpression("comments.commentBuildUsername").as("commentBuildUsername")
                .andExpression("comments.commentContent").as("commentContent")
                .andExpression("comments.replyCount").as("replyCount")
                .andExpression("comments.hotValue").as("hotValue")
                .andExpression("comments.commentStatus").as("commentStatus")
                .andExpression("comments.likeNumber").as("likeNumber")
                .andExpression("comments.dislikeNumber").as("dislikeNumber")
                .andExpression("comments.buildDate").as("buildDate"));
        Aggregation aggregation = Aggregation.newAggregation(operations);

        // 查询结果
        AggregationResults<CommentEntity> results = mongoTemplate.aggregate(aggregation, getCollectionName(mainType), CommentEntity.class);
        CommentEntity commentEntity = results.getMappedResults().get(0);
        commentEntity.setReplyCommentList(null);
        return commentEntity;
    }

    //获取指定分类和绑定ID下的指定评论回复的详细信息
    public CommentReplyEntity getCommentReplyByCommentReplyID(Integer bindID, MainType mainType, String commentReplyID) {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("bindID").is(bindID)));
        operations.add(Aggregation.unwind("comments"));
        operations.add(Aggregation.unwind("comments.replyCommentList"));
        operations.add(Aggregation.match(Criteria.where("comments.replyCommentList._id").is(commentReplyID)));
        operations.add(Aggregation.project("replyBuildUsername", "commentContent", "likeNumber", "dislikeNumber", "buildDate")
                .andExpression("comments.replyCommentList._id").as("_id")
                .andExpression("comments.replyCommentList.replyBuildUsername").as("replyBuildUsername")
                .andExpression("comments.replyCommentList.replyToUsername").as("replyToUsername")
                .andExpression("comments.replyCommentList.commentContent").as("commentContent")
                .andExpression("comments.replyCommentList.commentStatus").as("commentStatus")
                .andExpression("comments.replyCommentList.likeNumber").as("likeNumber")
                .andExpression("comments.replyCommentList.dislikeNumber").as("dislikeNumber")
                .andExpression("comments.replyCommentList.buildDate").as("buildDate"));
        Aggregation aggregation = Aggregation.newAggregation(operations);

        // 查询结果
        AggregationResults<CommentReplyEntity> results = mongoTemplate.aggregate(aggregation, getCollectionName(mainType), CommentReplyEntity.class);
        CommentReplyEntity commentReplyEntity = results.getMappedResults().get(0);
        return commentReplyEntity;
    }

    //在指定分类和绑定ID下添加一条评论
    public void addComment(Integer bindID, MainType mainType,
                           CommentEntity fileCommentEntity) {
        Query query = new Query();
        query.addCriteria(Criteria.where("bindID").is(bindID));
        Update update = new Update();
        update.addToSet("comments", fileCommentEntity);

        CommentsListEntity commentsListEntity = getCommentsListEntity(bindID, mainType);
        update.set("commentCount", commentsListEntity.getCommentCount() + 1);
        mongoTemplate.upsert(query, update, CommentEntity.class, getCollectionName(mainType));
    }

    //在指定分类和绑定ID下的指定评论下添加一条回复
    public void addCommentReply(Integer bindID, MainType mainType,
                                String replyInCommentID, CommentReplyEntity fileCommentReplyEntity) {
        Query query = new Query();
        query.addCriteria(Criteria.where("bindID").is(bindID).and("comments._id").is(replyInCommentID));
        Update update = new Update();
        update.addToSet("comments.$.replyCommentList", fileCommentReplyEntity);

        CommentEntity commentEntity = getCommentByCommentID(bindID, mainType, replyInCommentID);
        update.set("comments.$.replyCount", commentEntity.getReplyCount() + 1);
        mongoTemplate.upsert(query, update, CommentEntity.class, getCollectionName(mainType));
    }

    //修改评论或回复可见性（或删除相关回复）
    public void changeCommentOrReplyStatus(Integer bindID, MainType mainType, String commentID, CommentStatusType newCommentStatus) {
        Update update = new Update();
        Query query;
        //判断是评论的ID还是回复的ID
        if (commentID.contains("_")) {
            switch (newCommentStatus) {
                case USER_DELETE:
                    //真的执行删除操作
                    query = Query.query(Criteria.where("bindID").is(bindID)
                            .and("comments._id").is(commentID.substring(0, commentID.indexOf("_"))));
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("_id", commentID);
                    update.pull("comments.$.replyCommentList", jsonObject);
                    //扣除数量
                    CommentEntity commentEntity = getCommentByCommentID(bindID, mainType, commentID.substring(0, commentID.indexOf("_")));
                    update.set("comments.$.replyCount", commentEntity.getReplyCount() - 1);
                    break;
                default:
                    query = Query.query(Criteria.where("bindID").is(bindID).and("comments._id").is(commentID));
                    update.set("comments.$[idx0].replyCommentList.$[idx1].commentStatus", newCommentStatus);
                    update.filterArray(Criteria.where("idx0.replyCommentList").exists(true));
                    update.filterArray(Criteria.where("idx1._id").is(commentID));
            }
        } else {
            query = Query.query(Criteria.where("bindID").is(bindID).and("comments._id").is(commentID));
            update.set("comments.$.commentStatus", newCommentStatus);
        }
        mongoTemplate.upsert(query, update, CommentEntity.class, getCollectionName(mainType));
    }

    public enum CommentEvaluateType {
        LIKE, DISLIKE, RECOMMEND;
    }

    //修改评论或回复评价数值
    private void changeCommentOrReplyEvaluateNumber(Integer bindID, MainType mainType, String commentID,
                                                   CommentEvaluateType evaluateType, Integer changeNumber) {
        Update update = new Update();
        Query query;
        //判断是评论的ID还是回复的ID
        if (commentID.contains("_")) {
            query = Query.query(Criteria.where("bindID").is(bindID));
            CommentReplyEntity commentReplyEntity = getCommentReplyByCommentReplyID(bindID, mainType, commentID);
            switch (evaluateType) {
                case LIKE:
                    update.set("comments.$[idx0].replyCommentList.$[idx1].likeNumber", commentReplyEntity.getLikeNumber() + changeNumber);
                    break;
                case DISLIKE:
                    update.set("comments.$[idx0].replyCommentList.$[idx1].dislikeNumber", commentReplyEntity.getDislikeNumber() + changeNumber);
                    break;
                case RECOMMEND:
                    return;
            }
            update.filterArray(Criteria.where("idx0.replyCommentList").exists(true));
            update.filterArray(Criteria.where("idx1._id").is(commentID));
        } else {
            query = Query.query(Criteria.where("bindID").is(bindID).and("comments._id").is(commentID));
            CommentEntity commentEntity = getCommentByCommentID(bindID, mainType, commentID);
            switch (evaluateType) {
                case LIKE:
                    update.set("comments.$.likeNumber", commentEntity.getLikeNumber() + changeNumber);
                    break;
                case DISLIKE:
                    update.set("comments.$.dislikeNumber", commentEntity.getDislikeNumber() + changeNumber);
                    break;
                case RECOMMEND:
                    update.set("comments.$.hotValue", commentEntity.getHotValue() + changeNumber);
                    break;
            }
        }
        mongoTemplate.upsert(query, update, CommentEntity.class, getCollectionName(mainType));
    }
    public void evaluateComment(Integer bindID, MainType mainType, String commentID, List<EvaluateType> evaluates) {
        for (EvaluateType evaluate:evaluates){
            switch (evaluate){
                case CANCEL_LIKE:
                    changeCommentOrReplyEvaluateNumber(bindID, mainType, commentID, CommentEvaluateType.LIKE,-1);
                    break;
                case LIKE:
                    changeCommentOrReplyEvaluateNumber(bindID, mainType, commentID, CommentEvaluateType.LIKE,+1);
                    break;
                case CANCEL_DISLIKE:
                    changeCommentOrReplyEvaluateNumber(bindID, mainType, commentID, CommentEvaluateType.DISLIKE,-1);
                    break;
                case DISLIKE:
                    changeCommentOrReplyEvaluateNumber(bindID, mainType, commentID, CommentEvaluateType.DISLIKE,+1);
                    break;
            }
        }
    }

    public void setFileComments(CommentsListEntity commentsListEntity, MainType mainType) {
        mongoTemplate.save(commentsListEntity, getCollectionName(mainType));
    }

    public CommentsListEntity initCommentArea(Integer bindID, MainType mainType) {
        CommentsListEntity commentsListEntity = new CommentsListEntity(bindID, mainType);
        switch (mainType) {
            case DOCUMENT:
                setFileComments(commentsListEntity, mainType);
                break;
            case NOTE:
                break;
        }
        return commentsListEntity;
    }

}
