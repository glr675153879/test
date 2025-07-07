package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Administrator
 */
@Data
@Schema(description = "转科数据保存")
public class KpiTransferSaveDTO {
    @Schema(description = "结果id")
    private Long id;

    @Schema(description = "科室id")
    private Long deptId;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType = "1";

    @Schema(description = "结果id字符串，逗号隔开")
    private String ids;
}
