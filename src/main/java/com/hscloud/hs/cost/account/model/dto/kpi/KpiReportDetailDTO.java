package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class KpiReportDetailDTO {
    private List<KpiCodeDetailDTO> list;
    @Schema(description = "接口code")
    private String code;
}
