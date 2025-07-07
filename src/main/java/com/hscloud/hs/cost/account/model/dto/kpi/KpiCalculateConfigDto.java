package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Accessors(chain = true)
public class KpiCalculateConfigDto {
    @Schema(description = "核算单元类型")
    private String unitType;
    @Schema(description = "人员类型")
    private String userType;
    @Schema(description = "科室人员类型")
    private String deptUserType;
    @Schema(description = "核算组别")
    private String groupName;

    private String resultJson;
    private String imputationType;

    private String id;
    private String imputationCode;

    @Schema(description = "周期")
    private Long period;

    @Schema(description = "")
    private String planChildCode;

    @Schema(description = "")
    private String planCode;

    @Schema(description = "")
    private Long taskChildId;

    @Schema(description = "code")
    private String code;

    @Schema(description = "name")
    private String name;

    @Schema(description = "value")
    private BigDecimal value;

    @Schema(description = "")
    private Long deptId;

    @Schema(description = "")
    private String empId;

    @Schema(description = "")
    private Long userId;

    @Schema(description = "")
    private String userName;

    @Schema(description = "")
    private String deptName;

    @Schema(description = "1,核算项，2，指标")
    private String type;
    @Schema(description = "4,固定值")
    private String caliber;

    private String userImp;
}
