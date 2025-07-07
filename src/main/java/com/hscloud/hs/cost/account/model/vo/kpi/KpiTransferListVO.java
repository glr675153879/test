package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 转科
 * @author Administrator
 */
@Data
@Schema(description = "转科数据处理列表")
public class KpiTransferListVO {
    @Schema(description = "周期")
    private String period;

    @Schema(description = "人员id")
    private Long userId;

    @Schema(description = "人员名称")
    private String userName;

    @Schema(description = "科室名称")
    private String deptName;

    @Schema(description = "核算项名称")
    private String itemName;

    @Schema(description = "核算项code,逗号隔开")
    private String code;
}
