package com.hscloud.hs.cost.account.model.vo.imputation;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.hscloud.hs.cost.account.model.dto.CommonDTO;
import com.hscloud.hs.cost.account.model.dto.CostUnitRelateInfoDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.CostUnitExcludedInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author YJM
 * @date 2023-09-05 11:13
 */
@Data
@Schema(description = "核算单元")
public class CostAccountUnitVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "科室单元名称")
    private String accountUnitName;

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


    @Schema(description = "删除标记，0未删除，1已删除")
    private String delFlag;

    @Schema(description = "初始化状态: Y:已初始化  N:未初始化")
    private String initialized;


    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "修改人")
    private String updateBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "租户id")
    private Long tenantId;


    public static CostAccountUnitVO build(CostAccountUnit costAccountUnit) {
        CostAccountUnitVO costAccountUnitVO = new CostAccountUnitVO();
        BeanUtils.copyProperties(costAccountUnit, costAccountUnitVO);
        costAccountUnitVO.setAccountUnitName(costAccountUnit.getName());
        return costAccountUnitVO;
    }
}
