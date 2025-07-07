package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KpiReportMemberDto {
    private List<Long> memberList = new ArrayList<>();
    private List<Long> excludesList = new ArrayList<>();

}
