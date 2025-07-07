package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author banana
 * @create 2023-09-18 18:56
 */
@Data
@Schema(description = "人员相关信息")
public class UserInfo {

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "工号")
    private String jobNumber;


    @Schema(description = "科室")
    private String dept;
}
