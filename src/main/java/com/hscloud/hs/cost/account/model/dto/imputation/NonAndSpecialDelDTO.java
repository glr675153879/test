package com.hscloud.hs.cost.account.model.dto.imputation;

import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/24 16:19
 */
@Data
@Schema(description = "特殊归集人员和不计收入人员删除")
public class NonAndSpecialDelDTO {

    @Schema(description = "归集主档ID")
    private Long imputationId;

    @NotNull(message = "ID不能为空")
    @Schema(description = "记录ID")
    private Long id;

    @NotNull(message = "月份不能为空")
    @Schema(description = "月份")
    private String cycle;
}
