package com.hscloud.hs.cost.account.model.dto.deptCost;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author zyj
 * @date 2024/09/19
 */
@Data
public class DcSysSoftwareDto {

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "软件名称")
    @NotBlank(message = "软件名称不能为空")
    private String softName;

    @Schema(description = "软件资产编号")
    @NotBlank(message = "软件资产编号不能为空")
    private String softCode;

    @Schema(description = "周期")
    @NotBlank(message = "周期不能为空")
    private String cycle;

    @Schema(description = "科室id")
    @NotBlank(message = "科室id不能为空")
    private String deptId;

    @Schema(description = "科室名称")
    @NotBlank(message = "科室名称不能为空")
    private String deptName;

    /**
     * AccountUnit的json数组格式
     * {@link AccountUnit}
     */
    @Schema(description = "分摊核算单元（json）")
    @NotBlank(message = "分摊核算单元不能为空")
    private String apportionmentUnits;

    @Schema(description = "购入价格")
    @NotBlank(message = "购入价格不能为空")
    private BigDecimal purchasePrice;

    @Schema(description = "购入日期")
    @NotBlank(message = "购入日期不能为空")
    private LocalDateTime purchaseDate;

    @Schema(description = "使用日期")
    @NotBlank(message = "使用日期不能为空")
    private LocalDateTime useDate;

    @Schema(description = "状态")
    @NotBlank(message = "状态不能为空")
    private String status;

    @Schema(description = "默认折旧月数")
    @NotBlank(message = "默认折旧月数不能为空")
    private String defaultDepreciationMonths;

    @Schema(description = "待摊销月数")
    @NotBlank(message = "待摊销月数不能为空")
    private BigDecimal monthAmortized;

    @Schema(description = "月折旧额")
    @NotBlank(message = "月折旧额不能为空")
    private BigDecimal monthlyDepreciation;



    @Getter
    @AllArgsConstructor
    public static enum StatusEnum {

        ENABLE("0", "启用"),

        FORBIDDEN("1", "禁用");

        private String val;

        private String desc;
    }

    /**
     * 核算单元
     */
    @Data
    public static class AccountUnit {

        private String code;

        private String name;

        private String value;

        private String showCode;
    }

}
