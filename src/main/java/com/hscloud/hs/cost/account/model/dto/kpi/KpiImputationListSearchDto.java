package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

/**
 * @Classname KpiImputationListSearchDto
 * @Description TODO
 * @Date 2024-09-18 10:52
 * @Created by sch
 */
@Data
public class KpiImputationListSearchDto {


    private String name;


    private String CategoryCode;


    private String busiType = "1";

    private Long period;
}
