package com.oriole.ocean.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.oriole.ocean.common.po.mysql.NoteEntity;
import com.oriole.ocean.common.po.mysql.NoteTypeEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.NoteService;
import com.oriole.ocean.service.NoteTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/noteService")
public class NoteController {

    @Autowired
    NoteService noteService;

    @Autowired
    NoteTypeService noteTypeService;

    @RequestMapping(value = "/getAllNoteType",method = RequestMethod.GET)
    public MsgEntity<List<NoteTypeEntity>> getAllNoteType() {
        return new MsgEntity<>("SUCCESS", "1", noteTypeService.getAllNoteType());
    }

    @RequestMapping(value = "/getNoteByNoteType",method = RequestMethod.GET)
    public MsgEntity<PageInfo<NoteEntity>> getNoteByNoteType(@RequestParam(required = false) Integer noteType,@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize, true);
        List<NoteEntity> noteEntityList = noteService.getNoteByNoteType(noteType);
        PageInfo<NoteEntity> pageInfo = new PageInfo<>(noteEntityList);
        return new MsgEntity<>("SUCCESS", "1", pageInfo);
    }
}
