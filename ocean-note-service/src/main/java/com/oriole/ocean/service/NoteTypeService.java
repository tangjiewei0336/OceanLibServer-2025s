package com.oriole.ocean.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oriole.ocean.dao.NoteTypeDao;
import com.oriole.ocean.common.po.mysql.NoteTypeEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NoteTypeService extends ServiceImpl<NoteTypeDao, NoteTypeEntity> {
    public List<NoteTypeEntity> getAllNoteType(){
        return list();
    }
}
