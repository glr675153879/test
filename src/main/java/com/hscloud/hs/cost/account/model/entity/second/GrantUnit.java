package com.hscloud.hs.cost.account.model.entity.second;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountUnitInfo;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import com.hscloud.hs.cost.account.model.pojo.DeptInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author hf
 * @since 2023-10-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "发放单元")
@TableName("sec_grant_unit")
public class GrantUnit extends BaseEntity<GrantUnit> {

    private static final long serialVersionUID = 1L;


    @Column(comment = "名称")
    @Schema(description = "名称")
    private String name;

    @Column(comment = "科室单元(含编外)")
    @Schema(description = "科室单元 1,2,3")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String ksUnitIds;

    @Column(comment = "科室单元名称")
    @Schema(description = "科室单元名称 a,b,c")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String ksUnitNames;

    @Column(comment = "科室单元(不含编外)")
    @Schema(description = "科室单元 1,2,3")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String ksUnitIdsNonStaff;

    @Column(comment = "科室单元名称(不含编外)")
    @Schema(description = "科室单元名称 a,b,c")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String ksUnitNamesNonStaff;

    @Column(comment = "负责人id")
    @Schema(description = "负责人id 1,2,3")
    private String leaderIds;

    @Column(comment = "负责人名称")
    @Schema(description = "负责人名称 a,b,c")
    private String leaderNames;

    @Column(comment = "extraUserIds")
    @Schema(description = "额外新增人员")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String extraUserIds;

    @Column(comment = "extraUserNames")
    @Schema(description = "额外新增人员")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String extraUserNames;

    @Column(comment = "是否初始化")
    @Schema(description = "是否初始化")
    private String ifInit;

    @Column(comment = "是否上报方式")
    @Schema(description = "是否上报方式")
    private String ifUpload;


    @Column(comment = "是否禁用 0启用 1禁用")
    @Schema(description = "是否禁用 0启用 1禁用")
    private String status = "0";

    @Schema(description = "负责人User")
    @TableField(exist = false)
    private Map<String,Object> leaderUser;

    @TableField(exist = false)
    private List<KpiAccountUnitInfo> depts;
}
