package com.hscloud.hs.cost.account.model.dto.kpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author Administrator
 */
@Data
@Schema(description = "核算项新增")
public class KpiItemDTO {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "核算项名称")
    private String itemName;

    @Schema(description = "分组code")
    private String categoryCode;

    @Schema(description = "采集方式,1,sql,2手工")
    private String acqMethod;

    @Schema(description = "手工上报项id")
    private Long reportId;

    @Schema(description = "指标保留小数")
    private Integer retainDecimal;

    @Schema(description = "进位规则 1四舍五入 2向上取整 3向下取整")
    private String carryRule;

    @Schema(description = "出参字段合集")
    private String accountObject;

    @Schema(description = "配置信息")
    private String config;

    @Schema(description = "启停用标记，0启用，1停用")
    private String status;

    @Schema(description = "是否需要转科")
    private String changeFlag;

    @Schema(description = "口径颗粒度 1人2科室3归集4固定值5多条件")
    private String caliber;

    @Schema(description = "是否校验")
    private String checkStatus;

    @Schema(description = "是否多条件指标")
    private String conditionFlag;

    @Schema(description = "是否用于病区借床分摊")
    private String bedsFlag;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "上一次测试用例")
    private String extTemplate;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType = "1";

    @Schema(description = "是否项目成本")
    private String projectFlag;

    @Schema(description = "是否用于二次分配")
    private String secondFlag;

    @Schema(description = "基础表ids")
    private String tableIds;

    @Schema(description = "查询条件列表")
    private List<KpiItemCondDto> condList;

    @Schema(description = "转科逻辑版本 0-旧版 1-新版")
    private String changeVersion;

    @Schema(description = "是否当量计算")
    private String equivalentFlag;

    @Schema(description = "是否当量分配")
    private String assignFlag;

    @Schema(description = "项目分类")
    private String proCategoryCode;

    @Schema(description = "项目类型")
    private String serviceItemCategoryCode;

    @Schema(description = "单位")
    private String unit;
}
