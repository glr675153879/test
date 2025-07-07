package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiCalculate;
import lombok.Data;

import java.util.List;

@Data
public class KpiCalculateGroupDto {
    private String code;
    private List<KpiCalculate> list;

    public KpiCalculateGroupDto(String code, List<KpiCalculate> list) {
        this.code = code;
        this.list = list;
    }

    public KpiCalculateGroupDto() {
    }
}
