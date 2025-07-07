package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 人员信息
 * </p>
 *
 * @author author
 * @since 2023-11-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("distribution_user_info")
@Schema(description ="人员信息")
public class DistributionUserInfo extends Model<DistributionUserInfo> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "人员id")
    private Long userId;

    @Schema(description = "人员名称")
    private String userName;

    @Schema(description = "科室单元id")
    private Long unitId;

    @Schema(description = "科室单元名称")
    private String unitName;

    @Schema(description = "人员类型")
    private String type;

    @Schema(description = "岗位职务")
    private String job;

    @Schema(description = "是否拿奖金 0 否 1 是")
    private String isBonus;

    @Schema(description = "是否是独立科室单元 0 否 1 是")
    private String isUnit;

    @Schema(description = "核算分组")
    private String accountGroupCode;


}
