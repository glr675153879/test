package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "指标适用对象")
public class KpiIndexPlanMemberEditDto {
    @Schema(description = "/kpiIndexFormula/planList中的planCode,是为左边分组")
    private String planCode;
    @Schema(description = "公式id")
    private Long formulaId;
    @Schema(description = "方案适用对象")
    private String memberIds;
    @Schema(description = "方案适用对象组")
    private String memberCategroyCodes;

    @Schema(description = "科室组")
    private String planObjAccountType;

    private String delflag="Y";

    private String excludePerson;

    private String excludeDept;
}
