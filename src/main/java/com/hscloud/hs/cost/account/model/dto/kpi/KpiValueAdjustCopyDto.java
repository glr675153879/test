package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Classname KpiValueAdjustDto
 * @Description TODO
 * @Date 2024/12/5 13:49
 * @Created by sch
 */
@Data
public class KpiValueAdjustCopyDto {

    private List<Long> ids;

    private Long period;


}
