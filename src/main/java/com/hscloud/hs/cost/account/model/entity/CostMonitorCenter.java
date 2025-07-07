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
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "监测中心")
@Table
@Entity
public class CostMonitorCenter extends Model<CostMonitorCenter> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "测试id")
    @Id
    private Long id;

    @Schema(description = "绩效收入")
    @Column(columnDefinition = "varchar(255) comment '绩效收入'")
    private String name;

    @Schema(description = "成本值")
    @Column(columnDefinition = "varchar(255) comment '成本值'")
    private String cost;

    @Schema(description = "目标值")
    @Column(columnDefinition = "varchar(255) comment '目标范围(元)'")
    private String targetScope;

    @Schema(description = "日期")
    @Column(columnDefinition = "varchar(255) comment '日期'")
    private String dateVal;

    @Schema(description = "种类id")
    @Column(columnDefinition = "varchar(255) comment '种类id'")
    private String categoryId;

    /**
     * 0-正常，1-删除
     */
    @Schema(description = "0:收入,1:成本")
    @Column(columnDefinition = "char(1) comment '0:收入,1:成本'")
    private String dataType;


    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @Column(columnDefinition = "datetime comment '创建时间'")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.UPDATE)
    @Column(columnDefinition = "datetime comment '修改时间'")
    private LocalDateTime updateTime;


    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    @Column(columnDefinition = "varchar(64) comment '创建人'")
    private String createBy;

    /**
     * 修改人
     */
    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "修改人")
    @Column(columnDefinition = "varchar(64) comment '修改人'")
    private String updateBy;

    /**
     * 0-正常，1-删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "删除标记,1:已删除,0:正常")
    @Column(columnDefinition = "char(1) comment '是否删除：0：未删除 1：删除'")
    private String delFlag;


}
