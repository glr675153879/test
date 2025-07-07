package com.hscloud.hs.cost.account.model.vo.second;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
/**
 *
 * @author  lian
 * @date  2024/5/22 16:40
 *
 */
@Data
@Schema(description = "职工绩效(包含员工是否编内人员信息)")
public class UserTypeVo {
    @Schema(description = "周期")
    private String cycle;

    @Schema(description = "userId")
    private String userId;

    @Schema(description = "用户姓名")
    private String userName;

    @Schema(description = "核算单元id")
    private String deptId;

    @Schema(description = "科室单元")
    private String deptName;

    @Schema(description = "一次绩效值")
    private String amt;

    @Schema(description = "一次管理绩效值")
    private String glAmt;

    @Schema(description = "Y编内人员  N编外人员")
    private String userType;
}
