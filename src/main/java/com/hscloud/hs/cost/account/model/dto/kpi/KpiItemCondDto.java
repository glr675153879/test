package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Pattern;
import java.util.List;

@Data
@Schema(description = "核算项查询条件")
public class KpiItemCondDto {
    @Schema(description = "表id")
    private Long tableId;

    @Schema(description = "表名")
    private String tableName;

    @Schema(description = "字段名")
    private String fieldName;

    @Schema(description = "字段值")
    private String fieldValue;

    @Schema(description = "字段值列表，in或not in时用")
    private List<String> fieldValueList;

    @Schema(description = "字段类型")
    private String fieldType;

    @Schema(description = "运算符，=,>,<,>=,<=,!=,like,in,not in,is null,is not null")
    @Pattern(regexp = "=|>|<|>=|<=|!=|like|in|not in|is null|is not null", message = "运算符只能是=,>,<,>=,<=,!=,like,in,not in,is null,is not null")
    private String operator;

    @Schema(description = "连接符，连接后一个条件用的")
    @Pattern(regexp = "and|or", message = "连接符只能是'and'或'or'")
    private String connector;

    @Schema(description = "类型 group-条件分组")
    private String type;

    @Schema(description = "查询条件列表")
    private List<KpiItemCondDto> data;
}
