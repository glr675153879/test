package com.hscloud.hs.cost.account.model.entity.imputation;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/17 17:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "归集指标")
@TableName("im_imputation_index")
public class ImputationIndex extends ImputationBaseEntity<ImputationIndex> {
    private static final long serialVersionUID = 1L;

    @Column(comment = "归集主档ID")
    @Schema(description = "归集主档ID")
    private Long imputationId;

    @Column(comment = "归集指标名称")
    @Schema(description = "归集指标名称")
    private String name;

    @TableField(exist = false)
    @Schema(description = "归集指标关联指标")
    private List<ImputationIndexDetails> indexDetails;



}
