package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "数据小组分摊信息")
public class DwsFinanceWardShare {

    @Schema(description = "护理单元id")
    private String nurseUnitId;

    @Schema(description = "护理单元名称")
    private String nurseUnitName;

    @Schema(description = "核算单元id")
    private String accountUnitId;

    @Schema(description = "核算单元名称")
    private String accountUnitName;

    @Schema(description = "核算项id")
    private Long accountItemId;

    @Schema(description = "分摊金额")
    private String shareFee;


}
