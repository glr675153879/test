package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResultCopy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpiItemResultCopyDTO extends KpiItemResultCopy {

    @Schema(description = "主刀医生")
    private String zdysName;

    @Schema(description = "病人科室")
    private String brksName;

    @Schema(description = "开医嘱医生/护士")
    private String kzysName;

    @Schema(description = "主刀医生科室")
    private String zdysksName;

    @Schema(description = "开医嘱医生科室")
    private String kzysksName;

    @Schema(description = "归集科室")
    private String imputationDeptIdName;

    @Schema(description = "病区")
    private String wardName;

    @Schema(description = "病人病区")
    private String brbqName;


}
