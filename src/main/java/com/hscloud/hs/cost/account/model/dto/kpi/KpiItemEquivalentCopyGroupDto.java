package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemEquivalentCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResultCopy;
import lombok.Data;

import java.util.List;

@Data
public class KpiItemEquivalentCopyGroupDto {
    private String code;
    private String caliber;
    private List<KpiItemEquivalentCopy> list;

    public KpiItemEquivalentCopyGroupDto(String code, List<KpiItemEquivalentCopy> list) {
        this.code = code;
        this.list = list;
    }

    public KpiItemEquivalentCopyGroupDto() {
    }
}
