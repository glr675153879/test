package com.hscloud.hs.cost.account.model.dto.kpi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LockDataDto2 {

    @JsonProperty("q[period]")
    private String period;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType = "1";

}
