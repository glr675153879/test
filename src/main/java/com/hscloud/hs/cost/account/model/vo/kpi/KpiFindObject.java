package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpiFindObject {
    @Schema(description = "当前方案适用对象")
    private String thisPlanObj;
    @Schema(description = "除当前方案外适用对象(无论是否已经配置在方案中)")
    private String otherObj;
}
