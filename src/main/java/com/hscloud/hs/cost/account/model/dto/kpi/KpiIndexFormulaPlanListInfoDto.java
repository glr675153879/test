package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
* 指标公式Model
* @author you
* @since 2024-09-09
*/

@Data
@Schema(description = "指标公式下获取对应方案列表")
public class KpiIndexFormulaPlanListInfoDto {

    @Schema(description = "指标公式id")
    private Long formulaId;

}