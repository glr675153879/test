package com.hscloud.hs.cost.account.model.entity.imputation;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.dto.imputation.SpecialPersonIndexOrUnitDTO;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/17 17:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "不计收入人员")
@TableName("im_non_income_person")
public class NonIncomePerson extends ImputationBaseEntity<NonIncomePerson> {
    private static final long serialVersionUID = 1L;

    @Column(comment = "归集主档ID")
    @Schema(description = "归集主档ID")
    private Long imputationId;


    @Column(comment = "科室单元/人 名称")
    @Schema(description = "科室单元/人 名称")
    private String deptOrUserName;


    @Column(comment = "科室单元/人 ID")
    @Schema(description = "科室单元/人 ID")
    private Long deptOrUserId;

    @Column(comment = "需计入收入人员")
    @Schema(description = "需计入收入人员")
    private String needIncomePersons;

    @Column(comment = "需计入收入人员id")
    @Schema(description = "需计入收入人员id")
    private String needIncomePersonIds;

    @Column(comment = "归集指标id")
    @Schema(description = "归集指标id")
    private String imputationIndexIds;

    @Column(comment = "归集指标名称")
    @Schema(description = "归集指标名称")
    private String imputationIndexNames;

    @Column(comment = "科室单元/人 类型 DEPT:科室，USER：人")
    @Schema(description = "科室单元/人 类型")
    private String deptOrUserType;

    @TableField(exist = false)
    @Schema(description = "归集指标id，name列表可能有多个归集指标")
    private List<SpecialPersonIndexOrUnitDTO> imputationIndexList;

    @TableField(exist = false)
    @Schema(description = "需计入收入人员信息")
    private Map<String, Object> leaderUser;

    @TableField(exist = false)
    @Schema(description = "科室")
    private Map<String, Object> leaderDept;


}
