package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import java.util.List;

@Data
public class PlanCodeMemberListDto {
    private String indexCode;
    private String planCode;
    private List<Long> memberList;

    public PlanCodeMemberListDto() {
    }

    public PlanCodeMemberListDto(String indexCode,String planCode, List<Long> memberList) {
        this.indexCode = indexCode;
        this.planCode = planCode;
        this.memberList = memberList;
    }
}
