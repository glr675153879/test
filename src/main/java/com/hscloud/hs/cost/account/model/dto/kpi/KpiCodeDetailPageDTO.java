package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpiCodeDetailPageDTO extends PageDto {
    @Schema(description = "出参编码")
    private String code;
    @Schema(description = "出参名称")
    private String name;
    @Schema(description = "口径")
    private String caliber;
    @Schema(description = "接口code")
    private String report_code;
}
