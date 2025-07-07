package com.hscloud.hs.cost.account.model.dto.kpi;

import lombok.Data;

import java.util.List;

/**
 * @Classname KpiUserFactorAddListDto
 * @Description TODO
 * @Date 2025/4/23 16:51
 * @Created by sch
 */
@Data
public class KpiUserFactorAddListDto {

    private Long userId;

    private List<KpiUserFactorAddDto> list;
}
