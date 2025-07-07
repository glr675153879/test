package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Admin
 */
@Data
@Schema(description = "核算指标计算信息")
public class AccountIndexCalculateInfo implements java.io.Serializable{

    @Schema(description = "核算指标名称")
    private String indexName;

    @Schema(description = "核算指标计算值")
    private BigDecimal indexCalculateValue;

    @Schema(description = "包含的计算信息")
    private List<AccountItemCalculateInfo> accountItemCalculateInfoList;

}
