package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiAllocationRuleCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexFormulaCopy;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KpiIndexCopyExt2 extends KpiIndexCopy {
    private String fid;
    //公式id
    private Long groupId;
    //公式
    private KpiIndexFormulaCopy formula;
    private KpiAllocationRuleCaDto formulaAllo;
    //有无计算完成
    private boolean finish = false;
    //人口径是否需要计算科室
    private boolean needUserDept = false;
    //适用对象 可以合并
    private List<Long> members = new ArrayList<>();
    //公式计算依赖
    private List<FormulateMemberDto> depends = new ArrayList<>();
}
