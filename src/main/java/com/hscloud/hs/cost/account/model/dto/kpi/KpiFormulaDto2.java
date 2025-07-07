package com.hscloud.hs.cost.account.model.dto.kpi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
public class KpiFormulaDto2{
    private String formulaImputaion;
    @JsonProperty("fields")
    private List<FieldsDTO> fields;
    @JsonProperty("formulaShow")
    private String formulaShow;
    @JsonProperty("formulaShow")
    private List<String> formulaShowX;
    @JsonProperty("formulaOrigin")
    private String formulaOrigin;
    @JsonProperty("alloType")
    private int allo_type;
    @JsonProperty("fieldList")
    private List<FieldListDTO> fieldList;
    @JsonProperty("memberList")
    private List<MemberListDTO> memberList;
    @JsonProperty("conditionList")
    private List<ConditionListDTO> conditionList;
    //@JsonProperty("fieldList")
    //private List<FieldListDTO> fieldListX;

    public String getFormulaOrigin() {
        return formulaOrigin.replace("MAX(","max(").replace("MIN(","min(");
    }

    @NoArgsConstructor
    @Data
    public static class FieldsDTO {
        @JsonProperty("caliber")
        private String caliber;
        @JsonProperty("fieldCode")
        private String fieldCode;
        @JsonProperty("fieldName")
        private String fieldName;
        @JsonProperty("fieldType")
        private String fieldType;
        @JsonProperty("paramCate")
        private String paramCate;
        @JsonProperty("paramType")
        private String paramType;
        @JsonProperty("paramValues")
        private List<MemberListDTO> paramValues;
        @JsonProperty("paramExcludes")
        private List<MemberListDTO> paramExcludes;
        @JsonProperty("paramDesc")
        private String paramDesc;
    }

    @NoArgsConstructor
    @Data
    public static class FieldListDTO {
        private String mateFlag;
        @JsonProperty("caliber")
        private String caliber;
        @JsonProperty("fieldCode")
        private String fieldCode;
        @JsonProperty("fieldName")
        private String fieldName;
        @JsonProperty("fieldType")
        private String fieldType;
        @JsonProperty("paramCate")
        private String paramCate;
        @JsonProperty("paramType")
        private String paramType;
        @JsonProperty("paramValues")
        private List<MemberListDTO> paramValues;
        @JsonProperty("paramDesc")
        private String paramDesc;
        @JsonProperty("paramExcludes")
        private List<MemberListDTO> paramExcludes;
        @JsonProperty("code")
        private String code;
        @JsonProperty("fieldValue")
        private BigDecimal fieldValue;
        @JsonProperty("formulaShow")
        private List<String> formulaShowX;
        @JsonProperty("formulaOrigin")
        private String formulaOrigin;
        @JsonProperty("allImpMembers")
        private List<Long> allImpMembers;
        @JsonProperty("fieldCodeExtra")
        private String fieldCodeExtra;
    }

    @NoArgsConstructor
    @Data
    public static class MemberListDTO {
        @JsonProperty("label")
        private String label;
        @JsonProperty("value")
        private String value;
        @JsonProperty("type")
        private String type;
    }

    @NoArgsConstructor
    @Data
    public static class ConditionListDTO {
        @JsonProperty("id")
        private Long id;
        @JsonProperty("key")
        private String key;
        @JsonProperty("value")
        private List<MemberListDTO> value;
        @JsonProperty("type")
        private String type;
        @JsonProperty("name")
        private String name;
        @JsonProperty("relation")
        private String relation;
    }
}
