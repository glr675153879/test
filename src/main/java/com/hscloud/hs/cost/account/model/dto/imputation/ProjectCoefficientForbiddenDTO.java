package com.hscloud.hs.cost.account.model.dto.imputation;

import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/19 16:49
 */
@Data
@Schema(description = "项目系数停用/禁用请求")
public class ProjectCoefficientForbiddenDTO {

    @NotNull(message = "ID不能为空")
    @Schema(description = "ID")
    private Long id;

    @NotNull(message = "启停用标记不能为空")
    @Schema(description = "启停用标记")
    private String status;
}
