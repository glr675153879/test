package com.hscloud.hs.cost.account.model.vo.kpi;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KpiTaskReportLeftVO {

    private List<KpiKeyValueVO> plans = new ArrayList<>();
}
