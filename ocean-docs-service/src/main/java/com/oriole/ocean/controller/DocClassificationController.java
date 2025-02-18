package com.oriole.ocean.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.oriole.ocean.common.po.mongo.IndexNodeEntity;
import com.oriole.ocean.common.po.mysql.FileEntity;
import com.oriole.ocean.common.po.mysql.FileTypeEntity;
import com.oriole.ocean.common.po.mysql.GroupEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.DocGroupServiceImpl;
import com.oriole.ocean.service.DocIndexServiceImpl;
import com.oriole.ocean.service.DocTypeServiceImpl;
import com.oriole.ocean.service.FileServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/docClassificationService")
public class DocClassificationController {

    @Autowired
    DocGroupServiceImpl docGroupService;
    @Autowired
    DocTypeServiceImpl docTypeService;
    @Autowired
    FileServiceImpl fileService;
    @Autowired
    DocIndexServiceImpl docIndexService;

    @RequestMapping(value = "/getIndexPageGroups",method = RequestMethod.GET)
    public MsgEntity<List<GroupEntity>> getAllGroups() {
        return new MsgEntity<>("SUCCESS", "1", docGroupService.getDocGroupList());
    }

    @RequestMapping(value = "/getTypesByTypeString",method = RequestMethod.GET)
    public MsgEntity<List<FileTypeEntity>> getTypesByTypeString(@RequestParam String typeString) {
        List<FileTypeEntity> fileTypeEntityList = docTypeService.getTypeListByTypeString(typeString);
        return new MsgEntity<>("SUCCESS", "1", fileTypeEntityList);
    }

    @RequestMapping(value = "/getGroupsFileList",method = RequestMethod.GET)
    public MsgEntity<PageInfo<FileEntity>> getFileListByTypeIDAndTagIDAndIndexString(
            @RequestParam(required = false) Integer typeID,
            @RequestParam(required = false) String tagString,
            @RequestParam(required = false) String indexString,
            @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize, true);
        List<FileEntity> fileEntityList = fileService.getFileDetailsInfoListByTypeIDAndTagStringAndIndexString(typeID, tagString, indexString);
        for (FileEntity fileEntity : fileEntityList) {
            fileEntity.setRealObjectName(null);
        }
        PageInfo<FileEntity> pageInfo = new PageInfo<>(fileEntityList);
        return new MsgEntity<>("SUCCESS", "1", pageInfo);
    }

    @RequestMapping(value = "/getIndexList",method = RequestMethod.GET)
    public MsgEntity<IndexNodeEntity> getIndex(@RequestParam String indexString) {
        IndexNodeEntity indexNodeEntity = docIndexService.getAll(indexString);
        return new MsgEntity<>("SUCCESS", "1", indexNodeEntity);
    }

}
