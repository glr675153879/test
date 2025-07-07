package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * @author Administrator
 */
@Data
@Schema(description = "核算单元DTO")
public class KpiAccountUnitDTO {
    @Schema(description = "核算单元ID")
    private Long id;

    @Schema(description = "科室单元名称")
    @NotBlank(message = "科室单元名称不能为空")
    private String name;

    @Schema(description = "核算分组代码")
    private String categoryCode;

    @Schema(description="三方编码" )
    private String thirdCode;

    @Schema(description = "核算类型")
    private String accountTypeCode;

    @Schema(description = "负责人，多人逗号隔开")
    private String responsiblePersonId;

    @Schema(description = "负责人类型dept | user | role")
    private String responsiblePersonType;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType = "1";

    @Schema(description = "科室人员类型")
    private String accountUserCode;

    @Schema(description = "分组代码")
    private String groupCode;

    @Schema(description = "核算组别")
    private String accountGroup;

    @Schema(description = "科别 1门诊 2病区")
    private String deptType;

    @Schema(description = "科室编码")
    private String unitCode;

    private BigDecimal factor;
}
