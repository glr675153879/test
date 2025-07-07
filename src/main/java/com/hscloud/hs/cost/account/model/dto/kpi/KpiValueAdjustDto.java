package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Classname KpiValueAdjustDto
 * @Description TODO
 * @Date 2024/12/5 13:49
 * @Created by sch
 */
@Data
public class KpiValueAdjustDto {

    private Long period;

    private String busiType;

    private Long id;

    private String type;

    private String code;

    private String operation;

    private BigDecimal value;


    private Long accountUnit;

    private Long userId;

    private String remark;
}
