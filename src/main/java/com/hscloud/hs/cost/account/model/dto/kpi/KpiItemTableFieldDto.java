package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "核算项基础表字段")
public class KpiItemTableFieldDto extends PageDto {

    private Long id;

    @Schema(description = "表id")
    @NotNull(message = "表id不能为空")
    private Long tableId;

    @Schema(description = "字段名")
    private String fieldName;

    @Schema(description = "字段注释")
    private String fieldComment;

    @Schema(description = "字段类型")
    private String fieldType;

    @Schema(description = "状态 0-启用 1-停用")
    private String status;

    @Schema(description = "字段字典编码")
    private String dictCode;
}
