package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author YJM
 * @date 2023-09-06 09:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "核算单元关联科室人员表")
public class CostUnitRelateInfo extends Model<CostUnitRelateInfo> {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "关联科室单元ID")
    private Long accountUnitId;

    @Schema(description = "科室/人 名称")
    private String name;

    @Schema(description = " ")
    private String type;

    @Schema(description = "关联的科室id/人员id")
    private String relateId;
}
