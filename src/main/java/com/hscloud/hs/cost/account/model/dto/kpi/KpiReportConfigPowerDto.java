package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KpiReportConfigPowerDto {
    private Long reportId;
    @Schema(description = "人员id 逗号分割")
    private String userIds;
    @Schema(description = "人员分组code 逗号分割")
    private String groupCodes;
    @Schema(description = "人对应看到科室的数组")
    private List<UserDeptDto> list = new ArrayList<>();
}
