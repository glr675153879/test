package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 任务执行结果-核算项
 *
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-17 16:49:08
 */
@Data
@Schema(description = "任务执行结果-异常核算项")
@TableName("cost_task_execute_result_exception_item")
@EqualsAndHashCode(callSuper = true)
public class CostTaskExecuteResultExceptionItem extends Model<CostTaskExecuteResultExceptionItem> {
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
     * 核算时间段
     */
    @Schema(description = "核算时间段")
    private String timePeriod;
    /**
     * 异常原因
     */
    @Schema(description = "异常原因")
    private String exceptionReason;

}
