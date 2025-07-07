package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KpiReportCodeDTO {

    @Schema(description = "周期 例202408")
    private Long cycle;
    @Schema(description = "是否编外 Y是/N否")
    private String userType;
    @Schema(description = "核算单元 科室 多个,分割")
    private String deptIds;
    @Schema(description = "核算单元 人 多个,分割")
    private String userIds;
    @Schema(description = "接口code")
    private String code;
    @Schema(description = "接口codes")
    private String indexCodes;
    @Schema(description = "核算项code")
    private String itemCodes;
    @Schema(description = "是否过滤值为0 Y是/N否")
    private String filterZero;
}
