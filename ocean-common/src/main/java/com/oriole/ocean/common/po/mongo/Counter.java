package com.oriole.ocean.common.po.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@lombok.Data
@Document("counters")
public class Counter {
    @Id
    private String id; // 如 "questions"
    private int seq;
}
