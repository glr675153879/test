package com.hscloud.hs.cost.account.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Schema(description = "异动变更对象")
public class CostDataChangeRecordDto {

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "规则")
    private String rule;

    @Schema(description = "生效时间")
    private LocalDateTime effectTime;

}
