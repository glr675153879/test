package com.hscloud.hs.cost.account.model.entity.imputation;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.dto.imputation.SpecialPersonIndexOrUnitDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
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
@Schema(description = "特殊归集人员")
@TableName("im_special_imputation_person")
public class SpecialImputationPerson extends ImputationBaseEntity<SpecialImputationPerson> {
    private static final long serialVersionUID = 1L;

    @Column(comment = "归集主档ID")
    @Schema(description = "归集主档ID")
    private Long imputationId;

    @Column(comment = "userid")
    @Schema(description = "userid")
    private Long userId;

    @Column(comment = "姓名")
    @Schema(description = "姓名")
    private String userName;

    @Column(comment = "归集原因")
    @Schema(description = "归集原因")
    private String imputationReason;

    @Column(comment = "归集指标id")
    @Schema(description = "归集指标id")
    private String imputationIndexIds;

    @Column(comment = "归集指标名称")
    @Schema(description = "归集指标名称")
    private String imputationIndexNames;

    @Column(comment = "科室单元id")
    @Schema(description = "科室单元id")
    private String accountUnitIds;

    @Column(comment = "归集科室单元")
    @Schema(description = "归集科室单元")
    private String accountUnitNames;

    @Column(comment = "专家等级")
    @Schema(description = "专家等级，字典")
    private String expertLevel;

    @TableField(exist = false)
    @Schema(description = "归集指标id，name列表， 收入归集可能有多个归集指标")
    private List<SpecialPersonIndexOrUnitDTO> imputationIndexList;

    @NotNull(message = "人员信息不能为空")
    @TableField(exist = false)
    @Schema(description = "人员信息")
    private Map<String, Object> leaderUser;

    @TableField(exist = false)
    @Schema(description = "科室信息")
    private Map<String, Object> leaderDept;

    @TableField(exist = false)
    @Schema(description = "操作人")
    private String operationName;

    @TableField(exist = false)
    @Schema(description = "操作时间")
    private LocalDateTime operationTime;

}
