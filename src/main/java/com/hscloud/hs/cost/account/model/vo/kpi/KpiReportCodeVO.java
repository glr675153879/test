package com.hscloud.hs.cost.account.model.vo.kpi;

import cn.hutool.json.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class KpiReportCodeVO {
    private List<JSONObject> list;
}
