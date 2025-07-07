package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 绩效分配报表目标值设置表
 * </p>
 *
 * @author author
 * @since 2023-11-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("distribution_statement_target_value")
@Schema(description="绩效分配报表目标值设置表")
public class DistributionStatementTargetValue extends Model<DistributionStatementTargetValue> {

    private static final long serialVersionUID = 1L;

     @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

     @Schema(description = "报表类型(枚举类)")
    private String statementTypy;

     @Schema(description = "目标值")
    private String number;

     @Schema(description = "时间(以月为单位)")
    private String time;


}
