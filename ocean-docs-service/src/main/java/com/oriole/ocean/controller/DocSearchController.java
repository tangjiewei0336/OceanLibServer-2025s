package com.oriole.ocean.controller;

import com.oriole.ocean.common.po.es.FileSearchEntity;
import com.oriole.ocean.common.vo.MsgEntity;
import com.oriole.ocean.service.ESearchServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;


@RestController
@Slf4j
@RequestMapping("/docSearchService")
public class DocSearchController {

    @Autowired
    ESearchServiceImpl eSearchService;

    @RequestMapping(value = "/search",method = RequestMethod.GET)
    public MsgEntity<SearchHits<FileSearchEntity>> searchDoc(@RequestParam String keywords, @RequestParam Integer page, @RequestParam Integer rows) {
        return new MsgEntity<>("SUCCESS", "1", eSearchService.searchFile(keywords, page, rows));
    }

    @RequestMapping(value = "/suggest",method = RequestMethod.GET)
    public MsgEntity<ArrayList<String>> suggestTitle(@RequestParam String keyword, @RequestParam Integer rows) {
        ArrayList<String> suggests = eSearchService.suggestTitle(keyword, rows);
        return new MsgEntity<>("SUCCESS", "1", suggests);
    }

}
