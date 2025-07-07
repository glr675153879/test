package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

/**
 * @Classname CalculationRuleInsertDto
 * @Description TODO
 * @Date 2025/1/7 14:50
 * @Created by sch
 */
@Data
public class CalculationRuleInsertDto {

    private Long id;

    private String json;

    private String busiType;

    private String status;

    //private Long period;
}
