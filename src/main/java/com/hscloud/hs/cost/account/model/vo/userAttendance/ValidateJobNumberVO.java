package com.hscloud.hs.cost.account.model.vo.userAttendance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 自定义字段
 *
 * @JC
 * @since 2024-05-29
 */
@Data
@Schema(description = "工号不存在信息")
public class ValidateJobNumberVO {
    private String empId;
    private String empName;
}
