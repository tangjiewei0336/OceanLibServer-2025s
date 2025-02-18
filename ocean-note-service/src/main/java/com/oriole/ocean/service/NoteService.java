package com.oriole.ocean.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.dao.NoteDao;
import com.oriole.ocean.common.po.mysql.NoteEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NoteService extends ServiceImpl<NoteDao, NoteEntity> {
    public List<NoteEntity> getNoteByNoteType(Integer noteType){
        QueryWrapper<NoteEntity> queryWrapper = new QueryWrapper<>();
        if(noteType!=null) {
            queryWrapper.eq("note_type", noteType);
        }
        return list(queryWrapper);
    }
}
