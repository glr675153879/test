package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "数据小组分摊信息")
public class AdsCostShareTempNew {


    @Schema(description = "核算年月")
    private String dt;

    @Schema(description = "一级指标名称")
    private String className;

    @Schema(description = "医生组id")
    private String accountUnitId;

    @Schema(description = "医生组名称")
    private String accountUnitName;

    @Schema(description = "核算单元类型,医生组，医技组，护理组")
    private Long accountType;

    @Schema(description = "核算项id")
    private Long itemId;

    @Schema(description = "核算项名称")
    private Long itemName;

    @Schema(description = "核算金额")
    private String accountFee;
}
