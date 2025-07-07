package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 小小w
 * @date 2023/12/1 10:43
 */

@Data
@Schema(description = "核算结果指标Vo(新)")
//数据小组
public class CostAccountTaskResultIndexProcessNewVo {

    @Schema(description = "核算总值")
    private String totalCount;

    @Schema(description = "核算总值")
    private Object details;

}
