package com.hscloud.hs.cost.account.model.dto.kpi;

import com.alibaba.fastjson.JSONObject;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignLeft;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignRight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class KpiSignDataDTO {
    @Schema(description = "左侧固定值")
    private KpiSignLeft left;

    @Schema(description = "右侧")
    private List<KpiSignRight> rights;

    @Schema(description = "字典配了绩效签发的 ")
    private List<JSONObject> dicts;
}
