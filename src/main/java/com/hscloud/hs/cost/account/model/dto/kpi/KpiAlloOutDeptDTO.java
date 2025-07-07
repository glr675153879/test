package com.hscloud.hs.cost.account.model.dto.kpi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class KpiAlloOutDeptDTO {
    @JsonProperty("out_dept")
    private List<String> outDept;
    @JsonProperty("out_dept_group")
    private List<String> outDeptGroup;
    @JsonProperty("out_dept_except")
    private List<String> outDeptExcept;
}
