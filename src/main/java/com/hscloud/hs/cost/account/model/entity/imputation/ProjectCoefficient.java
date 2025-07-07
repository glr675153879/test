package com.hscloud.hs.cost.account.model.entity.imputation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;


/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/17 17:52
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "项目系数")
@TableName("im_project_coefficient")
public class ProjectCoefficient extends BaseEntity<ProjectCoefficient> {
    private static final long serialVersionUID = 1L;

    @Length(min = 1, max = 255)
    @NotNull(message = "考核项目不能为空")
    @Column(comment = "考核项目")
    @Schema(description = "考核项目")
    private String assessmentProject;

    @Column(comment = "核算单元id")
    @Schema(description = "核算单元id")
    private String accountUnitId;

    @Length(min = 1, max = 255)
    @NotNull(message = "核算单元不能为空")
    @Column(comment = "核算单元")
    @Schema(description = "核算单元")
    private String accountUnitName;

    @Length(min = 1, max = 255)
    @Column(comment = "核算分组")
    @Schema(description = "核算分组")
    private String accountGroupCode;


    @NotNull(message = "项目标准不能为空")
    @Column(comment = "项目标准", decimalLength = 6, length = 15)
    @Schema(description = "项目标准")
    private BigDecimal projectStandard;

    @Column(comment = "启停用标记,0:启用，1：停用", length = 1, defaultValue = "0")
    @Schema(description = "启停用标记")
    private String status;


}
