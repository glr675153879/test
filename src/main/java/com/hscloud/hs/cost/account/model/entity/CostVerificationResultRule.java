package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author 小小w
 * @date 2023/10/31 13:28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema( description="定时任务分摊规则执行结果")
public class CostVerificationResultRule extends Model<CostVerificationResultRule> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 核算单元id
     */
    @Schema(description = "核算单元id")
    private Long unitId;
    /**
     * 分摊规则id
     */
    @Schema(description = "分摊规则id")
    private Long ruleId;
    /**
     * 核算时间段
     */
    @Schema(description = "核算时间段")
    private String accountDate;
    /**
     * 核算规则值
     */
    @Schema(description = "核算规则值")
    private BigDecimal ruleCount;
    
}
