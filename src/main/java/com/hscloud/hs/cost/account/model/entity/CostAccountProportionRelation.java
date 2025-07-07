package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 成本核算项关联
 * @author banana
 * @create 2023-09-13 15:04
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "成本核算项关联")
public class CostAccountProportionRelation extends Model<CostAccountProportionRelation> {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "核算比例id")
    private Long costAccountProportionId;

    @Schema(description = "业务id 根据选的核算范围/核算比例详情类型区分不同的id（1.科室单元id 2.科室id 3.人员id）")
    private String bzid;

    @Schema(description = "核算比例详情类型")
    private String type;

    @Schema(description = "核算比例详情内容")
    private String context;

    @Schema(description = "核算比例")
    private Double proportion;

    @Schema(description = "租户id")
    private Long tenantId;
}
