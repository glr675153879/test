package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Admin
 */
@Data
@Schema(description = "总的计算结果")
public class WholeAccountInfo implements java.io.Serializable{


    @Schema(description = "总核算值")
    private BigDecimal totalCost;

    @Schema(description = "核算指标计算信息")
    private List<AccountIndexCalculateInfo> accountIndexCalculateInfoList;


}
