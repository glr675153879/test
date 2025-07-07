package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpiCodePageDTO extends PageDto {
    @Schema(description = "接口code")
    private String code;
    @Schema(description = "接口名称")
    private String name;
    @Schema(description = "口径")
    private String caliber;
}
