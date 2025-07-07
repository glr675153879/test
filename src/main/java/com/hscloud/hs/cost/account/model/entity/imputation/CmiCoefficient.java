package com.hscloud.hs.cost.account.model.entity.imputation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@Schema(description = "CMI系数")
@TableName("im_cmi_coefficient")
public class CmiCoefficient extends BaseEntity<CmiCoefficient> {
    private static final long serialVersionUID = 1L;

    @Column(comment = "CMI区间小值", length = 10)
    @Schema(description = "CMI区间小值")
    private String min;

    @Column(comment = "CMI区间大值", length = 10)
    @Schema(description = "CMI区间大值")
    private String max;

    @Column(comment = "小值区间开闭标准，1：开，0：闭", length = 10)
    @Schema(description = "小值区间开闭标准，1：开，0：闭")
    private String minOnOff;

    @Column(comment = "大值区间开闭标准，1：开，0：闭", length = 10)
    @Schema(description = "大值区间开闭标准，1：开，0：闭")
    private String maxOnOff;

    @NotNull(message = "CMI系数不能为空")
    @Column(comment = "CMI系数", decimalLength = 6, length = 15)
    @Schema(description = "CMI系数")
    private BigDecimal coefficient;


}
