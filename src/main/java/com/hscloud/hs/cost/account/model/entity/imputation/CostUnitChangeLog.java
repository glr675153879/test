package com.hscloud.hs.cost.account.model.entity.imputation;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import java.time.LocalDateTime;

/**
 * @author tianbo
 * @version 1.0
 * @date 2024-08-09 15:44
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "核算单元变更日志")
@Entity
@TableName("cost_unit_change_log")
public class CostUnitChangeLog extends BaseEntity<CostUnitChangeLog> {

    @Column(comment = "操作类型", length = 10)
    @Schema(description = "操作类型")
    private String operationType;

    @Column(comment = "操作项", length = 10)
    @Schema(description = "操作项")
    private String operateItem;

    @Column(comment = "操作时间")
    @Schema(description = "操作时间")
    private LocalDateTime operationTime;

    @Column(comment = "操作人ID")
    @Schema(description = "操作人ID")
    private String operatorId;

    @Column(comment = "描述", length = 500)
    @Schema(description = "描述")
    private String description;

    @Column(comment = "类型 0：核算单元 1：归集单元")
    @Schema(description = "类型 0：核算单元 1：归集单元")
    private Integer type;

    @TableField(exist = false)
    @Schema(description = "操作人姓名")
    private String operatorName;

}
