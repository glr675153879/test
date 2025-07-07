package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendance;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendance;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "变动人员处理Dto2")
public class KpiUserAttendanceEditDto {

    //周期
    private String period;
    private List<KpiUserAttendance> customParams;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType = "1";
}
