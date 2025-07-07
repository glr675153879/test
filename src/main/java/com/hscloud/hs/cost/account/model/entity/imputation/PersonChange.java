package com.hscloud.hs.cost.account.model.entity.imputation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import groovy.lang.GString;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author tianbo
 * @Description：
 * @date 2024/8/6 11:22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "不计收入人员")
@TableName("im_person_change")
public class PersonChange extends BaseEntity<PersonChange> {

    @Column(comment = "归集周期", length = 20)
    @Schema(description = "归集周期")
    private String imputationCycle;

    @Column(comment = "操作类型", length = 10)
    @Schema(description = "操作类型")
    private String operationType;

    @Column(comment = "归集指标名称")
    @Schema(description = "归集指标名称")
    private String imputationIndexName;

    @Column(comment = "科室单元ID")
    @Schema(description = "科室单元ID")
    private Long accountUnitId;

    @Column(comment = "归集人员", type = MySqlTypeConstant.LONGTEXT)
    @Schema(description = "归集人员")
    private String userNames;

    @Column(comment = "归集人员ID", type = MySqlTypeConstant.LONGTEXT)
    @Schema(description = "归集人员ID")
    private String userIds;

}
