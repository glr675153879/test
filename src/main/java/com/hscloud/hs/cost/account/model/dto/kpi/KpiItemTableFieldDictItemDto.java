package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "核算项基础表字段字典值")
public class KpiItemTableFieldDictItemDto extends PageDto {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "字典编码")
    @NotNull(message = "字典编码不能为空")
    private String dictCode;

    @Schema(description = "字典值编码")
    @NotNull(message = "字典值编码不能为空")
    private String itemCode;

    @Schema(description = "字典值名称")
    private String itemName;

    @Schema(description = "字典值描述")
    private String itemDesc;

    @Schema(description = "状态 0-启用 1-停用")
    private String status;
}
