package com.oriole.ocean.common.po.mysql;

import lombok.Data;

import java.util.List;

@Data
public class UserCollectionEntity implements java.io.Serializable {

    @Data
    public class CollectionEntity{
        private String collectionID;
        private String name;
        private String desc;
        private Boolean isPublic;
        private List<Integer> items;
    }

    private String username;
    private List<CollectionEntity> collection;

    public UserCollectionEntity() {
    }

    public UserCollectionEntity(String username, List<CollectionEntity> collection) {
        this.username = username;
        this.collection = collection;
    }
}
