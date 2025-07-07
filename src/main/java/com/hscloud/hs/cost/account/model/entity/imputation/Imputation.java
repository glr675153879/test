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
@Schema(description = "归集主档")
@TableName("im_imputation")
public class Imputation extends ImputationBaseEntity<Imputation> {
    private static final long serialVersionUID = 1L;

    @Column(comment = "科室人员校验，科室：DEPT，人员：USER", length = 10)
    @Schema(description = "科室人员校验，科室：DEPT，人员：USER")
    private String keyType;

    @Column(comment = "是否锁定，0：未锁定，1：已锁定", length = 1, defaultValue = "0")
    @Schema(description = "是否锁定，0：未锁定，1：已锁定")
    private String lockFlag;


}
