package com.hscloud.hs.cost.account.model.entity;


import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务执行结果-核算项
 *
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-17 16:49:08
 */
@Data
@Schema(description = "任务执行结果-核算项")
@TableName("cost_task_execute_result_item")
@EqualsAndHashCode(callSuper = true)
public class CostTaskExecuteResultItem extends Model<CostTaskExecuteResultItem> {

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
     * 核算项id
     */
    @Schema(description = "核算项id")
    private Long itemId;
    /**
     * 核算指标id
     */
    @Schema(description = "核算指标id")
    private Long indexId;
    /**
     * 核算项名称
     */
    @Schema(description = "核算项名称")
    private String itemName;
    /**
     * 核算项路径
     */
    @Schema(description = "核算项路径")
    private String path;
    /**
     * 核算项key
     */
    @Schema(description = "核算项key")
    private String configKey;
    /**
     * 核算周期
     */
    @Schema(description = "核算周期")
    private String accountPeriod;
    /**
     * 被分摊核算对象
     */
    @Schema(description = "被分摊核算对象")
    private String accountObject;
    /**
     * 核算时间段
     */
    @Schema(description = "核算时间段")
    private String timePeriod;
    /**
     * 本单元核算值
     */
    @Schema(description = "本单元核算值")
    private BigDecimal calculateCount;

    @Schema(description = "最终核算值,乘以分摊比例后的数据")
    private BigDecimal finalCount;

    /**
     * 借床被分摊核算值
     */
    @Schema(description = "借床被分摊核算值")
    private BigDecimal bedBorrow;
    /**
     * 方案对应配置信息
     */
    @Schema(description = "方案对应配置信息")
    private String config;
    /**
     * 租户id
     */
    @Schema(description = "租户id")
    private Long tenantId;

}
