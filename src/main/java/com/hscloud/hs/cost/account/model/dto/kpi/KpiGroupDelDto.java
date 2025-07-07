package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Classname KpiGroupDelDto
 * @Description TODO
 * @Date 2024-09-10 10:06
 * @Created by sch
 */
@Data
public class KpiGroupDelDto {

    @Schema(description = "枚举值  user_group 人员分组  item_group  核算项分组  后续自补  imputation_type 归集管理分组")
    private String type;


    private Long id;
}
