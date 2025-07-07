package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "核算任务查询对象")
@EqualsAndHashCode(callSuper = true)
public class CostAccountTaskQueryDto extends PageDto{


    @Schema(description = "核算任务名称")
    private String accountTaskName;

    @Schema(description = "创建人")
    private String name;

    @Schema(description = "核算时间")
    private String accountTime;

    @Schema(description = "核算开始时间")
    private LocalDateTime accountStartTime;

    @Schema(description = "核算结束时间")
    private LocalDateTime accountEndTime;

    @Schema(description = "核算任务id")
    private Long id;


    @Schema(description = "核算类型")
    private String accountType;


    @Schema(description = "核算单元id集合")
    private String unitIds;

    @Schema(description = "归集单元id集合")
    private String collectionIds;

    @Schema(description = "核算方案id")
    private Long planId;


    @Schema(description = "核算维度 MONTH：月度 QUARTER：季度 YEAR：DAY：日度")
    private String dimension;

    @Schema(description = "具体维度")
    private String detailDim;

    @Schema(description = "是否支持统计 0：不支持 1：支持")
    private String supportStatistics;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "创建开始时间")
    private LocalDateTime createStartTime;

    @Schema(description = "创建结束时间")
    private LocalDateTime createEndTime;

    /**
     * 状态
     */
    @Schema(description = "状态 CALCULATING：计算中 EXCEPTION：异常 COMPLETED：已完成 TO_BE_SUBMITTED：待提交")
    private String status;
}
