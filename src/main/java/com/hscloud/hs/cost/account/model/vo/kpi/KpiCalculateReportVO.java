package com.hscloud.hs.cost.account.model.vo.kpi;

import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCalculate;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemEquivalent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class KpiCalculateReportVO {
    @Schema(description = "表头")
    private List<KpiKeyValueVO> head;
    @Schema(description = "计算结果")
    private List<KpiCalculate> calculates;
    @Schema(description = "核算项结果集")
    private List<KpiItemResultCopyDTO> itemResults;
    @Schema(description = "核算项项目分类指标 结果集")
    private List<KpiItemEquivalentCopyDTO> itemEquivalent;
    private BigDecimal sum;
    //P_ITEM("item","核算项"),
    //P_INDEX("index","指标"),
    //P_ALLOCATION("allocation","分摊"),
    @Schema(description = "item 核算项 index 指标 allocation分摊")
    private String type;
}
