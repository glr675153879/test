package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author banana
 * @create 2023-09-18 18:55
 */
@Data
@Schema(description = "科室相关信息")
public class DeptInfo {

    @Schema(description = "核算科室名称")
    private String dept;

    @Schema(description = "科室性质名称")
    private String deptType;
}
