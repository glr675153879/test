package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanConfigFormula;
import com.hscloud.hs.cost.account.model.entity.CostAccountPlanCost;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "核算方案配置")
public class CostAccountPlanQueryDto extends PageDto{

    @Schema(description = "id")
    private Long id;

    @Schema(description = "分组id")
    private Long planGroupId;

    @Schema(description = "核算方案名称")
    private String name;

    @Schema(description = "启停用标记，0启用，1停用")
    private String status;

    @Schema(description = "修改人")
    private String updateBy;

    @Schema(description = "修改人工号")
    private Integer updateByNumber;

    @Schema(description = "更新时间")
    private Date updateTime;

    //公式列表
    @Schema(description = "公式列表")
    private List<CostAccountPlanConfigFormula> listCostFormula;

    @Schema(description = "任务分组id")
    private Long taskGroupId;

    @Schema(description = "任务分组名称")
    private String taskGroupName;

    @Schema(description = "任务类型")
    private String taskType;

    @Schema(description = "核算对象 字典类型数据(科室单元/人员)")
    private String accountObject;

}
