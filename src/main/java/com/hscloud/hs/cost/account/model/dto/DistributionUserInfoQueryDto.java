package com.hscloud.hs.cost.account.model.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author 小小w
 * @date 2023/11/20 10:18
 */
@Data
@Schema(description = "人员信息")
public class DistributionUserInfoQueryDto extends PageDto {

    @Schema(description = "姓名")
    private String userName;

    @Schema(description = "工号")
    private Long userId;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "是否拿奖金")
    private String isBonus;

    @Schema(description = "科室单元名称")
    private String unitName;

    @Schema(description = "岗位职务")
    private String job;

    @Schema(description = "是否是独立科室单元 0 否 1 是")
    private String isUnit;

}

