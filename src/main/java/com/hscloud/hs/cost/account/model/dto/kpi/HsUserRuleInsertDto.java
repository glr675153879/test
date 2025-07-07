package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import me.chanjar.weixin.cp.bean.oa.applydata.ContentValue;

/**
 * @Classname CalculationRuleInsertDto
 * @Description TODO
 * @Date 2025/1/7 14:50
 * @Created by sch
 */
@Data
public class HsUserRuleInsertDto {

    private Long id;

    private String json;

    private String busiType;

    private String status;

    private String categoryCode;

    private Long period;
    //private Long period;

    @Schema(description = "是否移除不匹配人员   Y N")
    private String removeUnmatch = "Y";
}
