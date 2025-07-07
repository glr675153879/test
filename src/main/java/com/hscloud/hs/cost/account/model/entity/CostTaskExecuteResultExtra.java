package com.hscloud.hs.cost.account.model.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 任务执行结果-其他
 *
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-17 16:49:08
 */
@Data
@Schema(description = "任务执行结果-其他")
@TableName("cost_task_execute_result_extra")
@EqualsAndHashCode(callSuper = true)
public class CostTaskExecuteResultExtra extends Model<CostTaskExecuteResultExtra> {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 任务id
     */
    @Schema(description = "任务id")
    private Long taskId;
    /**
     * 核算单元id
     */
    @Schema(description = "核算单元id")
    private Long unitId;
    /**
     * 核算指标id
     */
    @Schema(description = "核算指标id")
    private Long indexId;
    /**
     * 核算项路径
     */
    @Schema(description = "核算项路径")
    private String path;
    /**
     * 被分摊核算单元id
     */
    @Schema(description = "被分摊核算单元id")
    private Long divideUnitId;


    @Schema(description = "被分摊核算单元名称")
    private String unitName;
    /**
     * 核算项id
     */
    @Schema(description = "核算项id")
    private Long itemId;
    /**
     * 分摊类型
     */
    @Schema(description = "类型 医护 yh 门诊 mz 借床 jc 病区 bq")
    private String divideType;
    /**
     * 分摊的原始核算值
     */
    @Schema(description = "分摊的原始核算值")
    private BigDecimal divideCountBefore;
    /**
     * 分摊的比例
     */
    @Schema(description = "分摊的比例")
    private String dividePercent;
    /**
     * 分摊后的核算值
     */
    @Schema(description = "分摊后的核算值")
    private BigDecimal divideCountAfter;
    /**
     * 租户id
     */
    @Schema(description = "租户id")
    private Long tenantId;

}
