package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.TableField;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.dto.PageDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Administrator
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "核算项列表查询")
public class KpiItemQueryDTO extends PageDto {
    @Schema(description = "分组code")
    private String categoryCode;

    @Schema(description = "核算项名称")
    private String itemName;

    @Schema(description = "启停用标记，0启用，1停用")
    private String status;

    @Schema(description = "颗粒度")
    private String caliber;

    @Schema(description = "是否多条件")
    private String conditionFlag;

    @Schema(description = "采集方式,1,sql,2手工")
    private String acqMethod;

    @Schema(description = "指标保留小数")
    private Integer retainDecimal;

    @Schema(description = "进位规则 1四舍五入 2向上取整 3向下取整")
    private String carryRule;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType = "1";

    @Schema(description = "是否用于病区借床分摊")
    private String bedsFlag;

    @Schema(description = "核算项code，逗号隔开")
    private String codes;

    @Schema(description = "计算状态 0未计算 1计算中 2已完成 8-已完成（0结果） 9计算异常")
    private String extStatus;

    @Schema(description = "是否用于二次分配")
    private String secondFlag;

    @Schema(description = "是否按当量计算 0-否 1-是")
    private String equivalentFlag;

    @Schema(description = "是否当量分配 0-否 1-是")
    private String assignFlag;

    @Schema(description = "是否需要转科")
    private String changeFlag;
}