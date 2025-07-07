package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignHead;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KpiSignDTO {

    private List<KpiSignDataDTO> data;
    @Schema(description = "表头")
    private List<KpiSignHead> head = new ArrayList<>();
}
