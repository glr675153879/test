package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.prometheus.client.Summary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 医护对应组关联表实体类
 * @author banana
 * @create 2023-09-11 14:31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "医护对应组关联表")
public class CostDocNRelation extends Model<CostDocNRelation> {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "医生组科室单元id")
    private Long docAccountGroupId;

    @Schema(description = "护士组科室单元id")
    private Long nurseAccountGroupId;

    @Schema(description = "租户id")
    private Long tenantId;
}
