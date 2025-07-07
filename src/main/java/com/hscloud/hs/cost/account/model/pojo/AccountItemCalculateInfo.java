package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Admin
 */
@Data
@Schema(description = "核算项计算信息")
public class AccountItemCalculateInfo implements java.io.Serializable {

    @Schema(description = "业务名称")
    private String bizName;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "业务计算值")
    private BigDecimal bizCalculateValue;
}
