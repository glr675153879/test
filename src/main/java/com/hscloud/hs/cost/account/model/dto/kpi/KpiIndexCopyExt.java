package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemCopy;
import lombok.Data;

import java.util.List;

@Data
public class KpiIndexCopyExt extends KpiIndexCopy {
    private List<FormulateMemberDto> list;
}
