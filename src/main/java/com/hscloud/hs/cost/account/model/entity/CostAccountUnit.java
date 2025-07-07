package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.hscloud.hs.cost.account.model.dto.CommonDTO;
import com.hscloud.hs.cost.account.model.dto.CostUnitRelateInfoDto;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author YJM
 * @date 2023-09-05 11:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "核算单元")
public class CostAccountUnit extends BaseEntity<CostAccountUnit> {

    @Schema(description = "科室单元名称")
    private String name;

    @Schema(description = "核算分组")
    private String accountGroupCode;

    @Schema(description = "核算类型")
    private String accountTypeCode;

    @Schema(description = "负责人ID ")
    private String responsiblePersonId;

    @Schema(description = "负责人姓名 ")
    private String responsiblePersonName;

    @Schema(description = "负责人类型 ")
    private String responsiblePersonType;

    @Schema(description = "启停用标记，0启用，1停用")
    private String status;

    @Schema(description = "初始化状态: Y:已初始化  N:未初始化")
    private String initialized;

    @TableField(exist = false)
    @Schema(description = "负责人")
    private CommonDTO responsiblePerson;

    @TableField(exist = false)
    @Schema(description = "核算科室/人列表")
    private List<CostUnitRelateInfoDto> costUnitRelateInfo;

    @TableField(exist = false)
    @Schema(description = "操作人")
    private String operationName;

    @TableField(exist = false)
    @Schema(description = "操作时间")
    private LocalDateTime operationTime;
}
