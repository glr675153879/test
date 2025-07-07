package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "核算项保存查询条件")
public class KpiItemSaveCondDto {

    @Schema(description = "核算项id")
    @NotNull(message = "核算项id不能为空")
    private Long itemId;

    @Schema(description = "查询条件列表")
    @Valid
    private List<KpiItemCondDto> condList;
}
