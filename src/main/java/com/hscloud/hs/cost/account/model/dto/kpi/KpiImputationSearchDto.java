package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Classname ImputationSearchDto
 * @Description TODO
 * @Date 2024-09-14 15:07
 * @Created by sch
 */
@Data
public class KpiImputationSearchDto extends PageDto {

    @Schema(description = "科室名称 中文")
    private String accountUnitName;

    @Schema(description = "核算组别 传code")
    private String accountGroup;

    private String categoryCode;

    private Long period;

    private String busiType="1";

    private String name;
}
