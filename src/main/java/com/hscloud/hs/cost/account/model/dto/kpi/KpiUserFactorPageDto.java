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
public class KpiUserFactorPageDto {


    @Schema(description = "表头")
    private List<KeyValueDTO> head_list;

    @Schema(description = "表格数据")
    private List<JSONObject> body_list;

//    private Long id;
//
//    @Schema(description = "科室code")
//    private String deptCode;
//
//    @Schema(description = "科室名称")
//    private String deptName;
//
//    @Schema(description = "name")
//    private String name;
//
//    @Schema(description = "职务中文")
//    private String office;
//
//    @Schema(description = "职务code")
//    private String officeCode;
//
//    @Schema(description = "用户Id")
//    private Long userId;

}
