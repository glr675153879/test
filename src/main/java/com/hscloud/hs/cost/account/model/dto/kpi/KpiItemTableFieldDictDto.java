package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "核算项基础表字段字典")
public class KpiItemTableFieldDictDto extends PageDto {

    private Long id;

    @Schema(description = "字典编码")
    @NotNull
    private String dictCode;

    @Schema(description = "字典名称")
    @NotNull
    private String dictName;

    @Schema(description = "字典描述")
    private String dictDesc;

    @Schema(description = "状态 0-启用 1-停用")
    private String status;
}
