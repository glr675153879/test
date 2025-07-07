package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "方案对应配置信息")
public class ConfigCorrespondenceItem {

    @Schema(description = "核算项id")
    private Long itemId;

    @Schema(description = "配置项id")
    private Long configId;

    @Schema(description = "配置项key")
    private String configKey;

    @Schema(description = "配置项名称")
    private String configName;

    @Schema(description = "配置项描述")
    private String configDesc;

    @Schema(description = "被分摊核算对象")
    private String accountObject;

    @Schema(description = "自定义科室,人员")
    private String customObject;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "保留小数")
    private Integer reservedDecimal;

    @Schema(description = "进位规则")
    private String carryRule;

    @Schema(description = "借床分摊标识")
    private String bedAllocation;

    @Schema(description = "医护分摊标识")
    private String docNurseAllocation;

    @Schema(description = "医护分摊比例")
    private String allocate;


    @Schema(description = "是否门诊分摊 0-否 1-是")
    private String outpatientPublic;

    @Schema(description = "病区成本分摊标记 0-否 1-是")
    private String wardCosts;

    @Schema(description = "核算值")
    private BigDecimal calculatedValue;


    @Schema(description = "核算明细")
    private List<ItemCalculateDetail> itemCalculateDetails;
}
