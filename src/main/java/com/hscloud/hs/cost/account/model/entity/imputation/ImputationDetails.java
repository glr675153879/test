package com.hscloud.hs.cost.account.model.entity.imputation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/18 8:56
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "归集明细")
@TableName("im_imputation_details")
public class ImputationDetails extends ImputationBaseEntity<ImputationDetails> {
    private static final long serialVersionUID = 1L;

    @Column(comment = "归集主档ID")
    @Schema(description = "归集主档ID")
    private Long imputationId;

    @Column(comment = "归集科室单元ID")
    @Schema(description = "归集科室单元ID")
    private Long imputationDeptUnitId;


    @Column(comment = "科室单元id")
    @Schema(description = "科室单元id")
    private Long accountUnitId;

    @Column(comment = "科室单元名称")
    @Schema(description = "科室单元名称")
    private String accountUnitName;

    @Column(comment = "归集指标id")
    @Schema(description = "归集指标id")
    private Long imputationIndexId;

    @Column(comment = "归集指标名称")
    @Schema(description = "归集指标名称")
    private String imputationIndexName;


    @Column(comment = "归集人员", type = MySqlTypeConstant.LONGTEXT)
    @Schema(description = "归集人员")
    private String empNames;

    @Column(comment = "归集人员ID", type = MySqlTypeConstant.LONGTEXT)
    @Schema(description = "归集人员ID")
    private String userIds;


}
