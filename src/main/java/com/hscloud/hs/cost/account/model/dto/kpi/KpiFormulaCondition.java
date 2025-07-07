package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.dto.DictDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class KpiFormulaCondition {
    @Schema(description = "zdys/brks/kzys")
    private String key;
    @Schema(description = "主刀医生/病人科室/开嘱医生")
    private String name;
    private Integer seq;
    @Schema(description = "等于/不等于")
    private String relation;
    private List<DictDto> value;
}
