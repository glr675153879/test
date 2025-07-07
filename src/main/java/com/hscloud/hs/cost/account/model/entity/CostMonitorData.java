package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 *
 * @author  lian
 * @date  2023-09-19 12:30
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "监测动态监测值测试数据")
public class CostMonitorData extends Model<CostMonitorData> {
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

    @Schema(description = "监测值")
    @Column(name = "monitor_value", columnDefinition = "varchar(255) comment '监测值'")
    private String monitorValue;

    @Schema(description = "监测日期")
    @Column(name = "monitor_date", columnDefinition = "varchar(255) comment '监测日期'")
    private String monitorDate;

    @Schema(description = "最新预警时间")
    @Column(name = "warn_time", columnDefinition = "varchar(255) comment '最新预警时间'")
    private String warnTime;

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
