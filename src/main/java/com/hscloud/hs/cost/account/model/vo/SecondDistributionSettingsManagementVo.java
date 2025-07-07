package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "二次分配设置管理vo")
public class SecondDistributionSettingsManagementVo {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "管理岗位")
    private String position;

    @Schema(description = "管理绩效金额")
    private String amount;

    @Schema(description = "计算单位")
    private String unit;

    @Schema(description = "是否为系统字段： 0：非系统字段；1：一次分配系统字段")
    private String isSystem;

    @Schema(description = "状态  0 启用  1 停用")
    private String status;

}
