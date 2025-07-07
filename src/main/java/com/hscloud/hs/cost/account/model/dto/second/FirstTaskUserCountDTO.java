package com.hscloud.hs.cost.account.model.dto.second;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "一次分配职工绩效总览")
public class FirstTaskUserCountDTO {

    @Schema(description = "周期")
    private String cycle;

    @Schema(description = "科室id")
    private String deptId;

    @Schema(description = "职工id")
    private Long userId;

    @Schema(description = "科室绩效")
    private String ksAmt;


}
