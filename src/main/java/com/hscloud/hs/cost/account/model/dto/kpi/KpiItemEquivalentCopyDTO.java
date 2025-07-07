package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemEquivalentCopy;
import lombok.Data;

@Data
public class KpiItemEquivalentCopyDTO extends KpiItemEquivalentCopy {
    private String caliber;
    private String name;
}
