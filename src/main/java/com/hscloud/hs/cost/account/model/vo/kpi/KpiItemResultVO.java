package com.hscloud.hs.cost.account.model.vo.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 */
@Data
@Schema(description = "核算项结果")
public class KpiItemResultVO {
    @Schema(description = "列表头数据")
    private List<String> headerList;

    @Schema(description = "列表数据")
    private List<List<String>> bodyList;
}
