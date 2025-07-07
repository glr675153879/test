package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author banana
 * @create 2023-09-13 20:33
 */
@Data
@Schema(description = "查询核算项的核算比例信息出参")
public class CostAccountProportionListVo {

    @Schema(description = "比例关联信息主键")
    private Long id;

    @Schema(description = "核算项名称")
    private String costAccountItem;

    /*// 科室单元
    @Schema(description = "科室单元名称")
    private String accountUnit;

    @Schema(description = "核算分组名称")
    private String typeGroup;

    // 科室
    @Schema(description = "核算科室名称")
    private String dept;

    @Schema(description = "科室性质名称")
    private String deptType;

    // 员工
    @Schema(description = "姓名")
    private String name;

    @Schema(description = "工号")
    private String jobNumber;*/

    @Schema(description = "核算比例详情内容")
    private String context;

    @Schema(description = "比例")
    private Double proportion;
}
