package com.oriole.ocean.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.common.enumerate.DocStatisticItemType;
import com.oriole.ocean.common.enumerate.EvaluateType;
import com.oriole.ocean.common.service.FileExtraService;
import com.oriole.ocean.dao.FileExtraDao;
import com.oriole.ocean.common.po.mysql.FileExtraEntity;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@DubboService
@Transactional
public class FileExtraServiceImpl extends ServiceImpl<FileExtraDao, FileExtraEntity> implements FileExtraService {

    public void evaluateDoc(Integer fileID, List<EvaluateType> evaluates) {
        for (EvaluateType evaluate : evaluates) {
            switch (evaluate) {
                case CANCEL_LIKE:
                    docBaseStatisticsInfoChange(fileID, "-1", DocStatisticItemType.LIKE);
                    break;
                case LIKE:
                    docBaseStatisticsInfoChange(fileID, "+1", DocStatisticItemType.LIKE);
                    break;
                case CANCEL_DISLIKE:
                    docBaseStatisticsInfoChange(fileID, "-1", DocStatisticItemType.DISLIKE);
                    break;
                case DISLIKE:
                    docBaseStatisticsInfoChange(fileID, "+1", DocStatisticItemType.DISLIKE);
                    break;
            }
        }
    }

    //文章基础统计信息更新
    public boolean docBaseStatisticsInfoChange(Integer fileID, String addNumber, DocStatisticItemType docStatisticItemType) {
        UpdateWrapper<FileExtraEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("file_id", fileID);
        switch (docStatisticItemType) {
            case DISLIKE:
                updateWrapper.setSql("`dislike_num` = `dislike_num`" + addNumber);
                break;
            case LIKE:
                updateWrapper.setSql("`like_num` = `like_num`" + addNumber);
                break;
            case READ:
                updateWrapper.setSql("`read_num` = `read_num`" + addNumber);
                break;
            case DOWNLOAD:
                updateWrapper.setSql("`download_num` = `download_num`" + addNumber);
                break;
            case COMMENT:
                updateWrapper.setSql("`comment_num` = `comment_num`" + addNumber);
                break;
            case COLLECTION:
                updateWrapper.setSql("`collection_num` = `collection_num`" + addNumber);
                break;
            default:
        }
        return update(updateWrapper);
    }

    // 文章分数更新
    public boolean docScoreChange(Integer fileID, Double score, Integer raters) {
        UpdateWrapper<FileExtraEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("file_id", fileID);
        updateWrapper.setSql("`score`=`score` + " + score.toString());
        updateWrapper.setSql("`raters_num`=`raters_num` + " + raters.toString());
        return update(updateWrapper);
    }

    //保存或更新文章附加信息
    public void saveOrUpdateFileExtraInfo(FileExtraEntity fileExtraEntity) {
        saveOrUpdate(fileExtraEntity);
    }
}
