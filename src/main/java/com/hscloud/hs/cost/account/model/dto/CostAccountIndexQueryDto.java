package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.entity.CostIndexConfigIndex;
import com.hscloud.hs.cost.account.model.vo.CostIndexConfigItemVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "核算指标查询对象")
@EqualsAndHashCode(callSuper = true)
public class CostAccountIndexQueryDto extends PageDto {

    @Schema(description = "核算指标id")
    private Long id;

    @Schema(description = "核算指标名称")
    private String name;

    @Schema(description = "指标分组id")
    private Long indexGroupId;

    @Schema(description = "核算指标状态")
    private String status;

    @Schema(description = "开始日期")
    private LocalDateTime beginTime;

    @Schema(description = "结束日期")
    private LocalDateTime endTime;


    @Schema(description = "指标单位")
    private String indexUnit;

    @Schema(description = "指标性质")
    private String indexProperty;

    @Schema(description = "统计周期")
    private String statisticalCycle;

    @Schema(description = "是否是系统指标  0 否  1 是")
    private String isSystemIndex;




    @Schema(description = "进位规则id")
    private String carryRule;

    @Schema(description = "指标保留小数")
    private Integer reservedDecimal;

    @Schema(description = "指标公式")
    private String indexFormula;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;

    @Schema(description = "核算项是核算指标的")
    private List<CostIndexConfigIndex> costIndexConfigIndexList;

    @Schema(description = "核算项是核算项的")
    private List<CostIndexConfigItemVo> costIndexConfigItemList;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 修改人
     */
    @Schema(description = "修改人")
    private String updateBy;

}
