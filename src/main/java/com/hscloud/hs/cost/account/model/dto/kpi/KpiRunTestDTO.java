package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpiRunTestDTO {
    private Long id;
    @Schema(description = "类型 1测试2正式")
    private int type;
    @Schema(description = "核算项计算")
    private boolean itemRefresh = true;

    @Schema(description = "人员归集")
    private boolean empRefresh = false;

    @Schema(description = "验证当量锁定")
    private boolean equivalent = false;
}
