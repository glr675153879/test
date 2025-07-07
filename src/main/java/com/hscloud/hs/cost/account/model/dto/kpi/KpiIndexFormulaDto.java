package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.pig4cloud.pigx.common.core.util.TenantTable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
* 指标公式Model
* @author you
* @since 2024-09-09
*/

@Data
@Schema(description = "指标公式表")
public class KpiIndexFormulaDto{

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "")
    private Long id;

    @TableField(value = "index_code")
    @Schema(description = "指标code")
    @NotBlank(message = "指标code不可空")
    private String indexCode;

    @Schema(description = "方案code")
    private String planCode;

    @TableField(value = "formula")
    @Schema(description = "公式")
    private List<String> formulas;

    @TableField(value = "show_flag")
    @Schema(description = "是否下转展示")
    private String showFlag;

    @TableField(value = "check_flag")
    @Schema(description = "是否校验")
    private String checkFlag;


    @Schema(description = "人/科室编码")
    private String memberIds;
}