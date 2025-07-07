package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "核算项基础表字段")
public class KpiItemTableFieldVO {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "表id")
    private Long tableId;

    @Schema(description = "表名")
    private String tableName;

    @Schema(description = "字段名")
    private String fieldName;

    @Schema(description = "字段注释")
    private String fieldComment;

    @Schema(description = "字段类型")
    private String fieldType;

    @Schema(description = "状态 0-启用 1-停用")
    private String status;

    @Schema(description = "字典编码")
    private String dictCode;

    @Schema(description = "字典名称")
    private String dictName;
}
