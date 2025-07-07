package com.hscloud.hs.cost.account.model.dto.kpi;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @Classname KpiCoefficientDto
 * @Description TODO
 * @Date 2024/11/27 13:41
 * @Created by sch
 */
@Data
public class KpiUserFactorBeforeDto {

    private Long id;

    @Schema(description = "科室Id")
    private String deptId;

    @Schema(description = "科室名称")
    private String deptName;

    @Schema(description = "name")
    private String name;

    @Schema(description = "职务中文")
    private String office;

    @Schema(description = "职务code")
    private String officeCode;

    @Schema(description = "用户Id")
    private Long userId;

    @Schema(description = "科室表id 用于匹配factor科室Id")
    private Long unitId;

    @Schema(description = "人员状态")
    private Long userStatus;

}
