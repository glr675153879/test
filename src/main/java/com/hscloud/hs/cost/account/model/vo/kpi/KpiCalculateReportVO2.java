package com.hscloud.hs.cost.account.model.vo.kpi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiCalculateConfigDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCalculate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class KpiCalculateReportVO2 {
    @Schema(description = "表头")
    private List<KpiKeyValueVO> head = new ArrayList<>();
    //    @Schema(description = "计算结果")
//    private List<KpiCalculateConfigDto> calculates;
//    @Schema(description = "计算结果")
//    private List<KpiCalculateConfigDto> last_calculates;
    @Schema(description = "计算结果")
    private JSONArray results = new JSONArray();
    @Schema(description = "计算结果")
    private List<KpiKeyValueVO2> sum = new ArrayList<>();

    //@Schema(description = "item 核算项 index 指标 allocation分摊")
    //private String type;
}
