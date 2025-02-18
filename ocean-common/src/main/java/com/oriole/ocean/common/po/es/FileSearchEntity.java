package com.oriole.ocean.common.po.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "file_search")
public class FileSearchEntity implements java.io.Serializable {

    @Id
    private Integer fileID;

    @Field(name = "analyzer_title", type = FieldType.Text, searchAnalyzer = "ik_max_word", analyzer = "ik_smart")
    private String title;

    @Field(name = "analyzer_abstract_content", type = FieldType.Text, searchAnalyzer = "ik_max_word", analyzer = "ik_smart")
    private String abstractContent;
    @Field(type = FieldType.Integer)
    private Integer size;
    @Field(name = "file_type", type = FieldType.Text)
    private String fileType;
    @Field(name = "upload_username", type = FieldType.Text)
    private String uploadUsername;

    @Field(name = "preview_picture_object_name", type = FieldType.Text)
    private String previewPictureObjectName;

    @Field(name = "payment_method", type = FieldType.Integer)
    private Integer paymentMethod;
    @Field(name = "payment_amount", type = FieldType.Integer)
    private Integer paymentAmount;

    @Field(name = "is_approved", type = FieldType.Boolean)
    private String isApproved;

    @Field(name = "hide_score", type = FieldType.Double)
    private Double hideScore;

    @Field(name = "analyzer_content",type = FieldType.Text, searchAnalyzer = "ik_max_word", analyzer = "ik_smart")
    private String content;
    @Field(name = "analyzer_keyword",type = FieldType.Keyword)
    private String keyword;

    @Field(name = "is_vip_income", type = FieldType.Text)
    private String isVipIncome;
    @Field(name = "score", type = FieldType.Text)
    private String score;
    @Field(name = "raters_num", type = FieldType.Text)
    private String ratersNum;
    @Field(name = "read_num", type = FieldType.Text)
    private String readNum;
}
