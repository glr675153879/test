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

/**
 * 任务执行结果-核算指标
 *
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-17 16:49:08
 */
@Data
@Schema(description = "任务执行结果-核算指标")
@TableName("cost_task_execute_result_index")
public class CostTaskExecuteResultIndex extends Model<CostTaskExecuteResultIndex> implements Serializable {
    private static final long serialVersionUID = 1L;

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
     * 核算指标计算的公式
     */
    @Schema(description = "核算指标计算的公式")
    private String calculateFormulaDesc;
    /**
     * 核算指标id
     */
    @Schema(description = "核算指标id")
    private Long indexId;
    /**
     * 核算指标名
     */
    @Schema(description = "核算指标名")
    private String indexName;
    /**
     * 指标核算值
     */
    @Schema(description = "指标核算值")
    private BigDecimal indexCount;
    /**
     * 指标核算值
     */
    @Schema(description = "没有分摊成本的指标核算值")
    private BigDecimal noExtraIndexCount;
    /**
     * 父级核算指标id
     */
    @Schema(description = "父级核算指标id")
    private Long parentId;
    /**
     * 路径
     */
    @Schema(description = "路径")
    private String path;
    /**
     * 计算明细
     */
    @Schema(description = "计算明细")
    private String calculateDetail;
    /**
     * 核算项id列表
     */
    @Schema(description = "核算项id列表")
    private String items;
    /**
     * 租户id
     */
    @Schema(description = "租户id")
    private Long tenantId;

}
