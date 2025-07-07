package com.hscloud.hs.cost.account.model.entity.imputation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/17 17:51
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "归集指标明细")
@TableName("im_imputation_index_details")
public class ImputationIndexDetails extends BaseEntity<ImputationIndexDetails> {
    private static final long serialVersionUID = 1L;

    @Column(comment = "归集指标Id")
    @Schema(description = "归集指标Id")
    private Long imputationIndexId;

    @Column(comment = "关联指标Id")
    @Schema(description = "关联指标Id")
    private Long costAccountIndexId;

    @Column(comment = "关联指标")
    @Schema(description = "关联指标")
    private String costAccountIndexName;


}
