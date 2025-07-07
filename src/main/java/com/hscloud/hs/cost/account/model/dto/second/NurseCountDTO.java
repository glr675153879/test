package com.hscloud.hs.cost.account.model.dto.second;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "护理绩效报表")
public class NurseCountDTO {

    @Schema(description = "周期")
    private String cycle;

    @Schema(description = "核算单元id")
    private String deptId;

    @Schema(description = "科室绩效")
    private String ksAmt;

    @Schema(description = "管理绩效")
    private String glAmt;

    @Schema(description = "护士长绩效")
    private String hszAmt;

}
