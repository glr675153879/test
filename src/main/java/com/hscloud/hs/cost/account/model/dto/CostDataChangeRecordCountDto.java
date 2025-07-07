package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "查询移动数量dto")
public class CostDataChangeRecordCountDto {

    @Schema(description = "业务类型")
    private String bizCode;

    @Schema(description = "业务类型id集合")
    private List<Long> bizIds;
}
