package com.hscloud.hs.cost.account.model.entity.imputation;

import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import com.hscloud.hs.cost.account.model.entity.base.Entity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.MappedSuperclass;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/18 15:50
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@MappedSuperclass
public abstract class ImputationBaseEntity<T extends BaseEntity<T>> extends BaseEntity<T> {

    @Column(comment = "归集名称")
    @Schema(description = "归集名称")
    private String imputationName;

    @Column(comment = "归集类型")
    @Schema(description = "归集类型")
    private String imputationCode;

    @Column(comment = "归集周期", length = 20)
    @Schema(description = "归集周期")
    private String imputationCycle;
}
