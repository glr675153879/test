package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 异常月份入库记录
 * @author  lian
 * @date  2023-09-19 12:30
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "异常月份记录")
@Entity
@Table(name = "cost_monitor_ab_month")
public class CostMonitorAbMonth extends Model<CostMonitorAbMonth> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    @Id
    private Long id;

    @Schema(description = "科室单元id")
    @Column(name = "unit_id", columnDefinition = "varchar(255) comment '科室单元id'")
    private String unitId;

    @Schema(description = "核算项id")
    @Column(name = "item_id", columnDefinition = "varchar(255) comment '核算项id'")
    private String itemId;

    @Schema(description = "月份")
    @Column(name = "month", columnDefinition = "varchar(255) comment '月份'")
    private String month;

    @Schema(description = "当月累计监测总值")
    private BigDecimal monitorValueMonth;

    @Schema(description = "警戒状态 0正常 1超出 2低于")
    private String status = "0";

    @Schema(description = "目标值")
    @Column(name = "target_value", columnDefinition = "varchar(255) comment '目标值'")
    private String targetValue;

    @Schema(description = "警戒值")
    private BigDecimal warnValue;

    @Schema(description = "单位")
    private String measureUnit;

    @Schema(description = "同比")
    private String sequentialGrowth;

    @Schema(description = "环比")
    private String yearOnYearGrowth;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @Column(name = "createTime", columnDefinition = "datetime comment '创建时间'")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.UPDATE)
    @Column(name = "updateTime", columnDefinition = "datetime comment '修改时间'")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    @Column(name = "create_by", columnDefinition = "varchar(255) comment '创建人'")
    private String createBy;

    /**
     * 修改人
     */
    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "修改人")
    @Column(name = "update_by", columnDefinition = "varchar(255) comment '修改人'")
    private String updateBy;

    /**
     * 0-正常，1-删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "删除标记,1:已删除,0:正常")
    @Column(name = "del_flag", columnDefinition = "char(1) comment '删除标记,1:已删除,0:正常'")
    private String delFlag;

}
