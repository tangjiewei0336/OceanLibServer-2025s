package com.oriole.ocean.common.po.mongo;

import lombok.Data;

import java.util.Map;

@Data
public class IndexNodeEntity implements java.io.Serializable {

    private String nodeName;
    private String nextNodeClassificationMethodName;
    private Map<Integer,IndexNodeEntity> nextNodes;
}
