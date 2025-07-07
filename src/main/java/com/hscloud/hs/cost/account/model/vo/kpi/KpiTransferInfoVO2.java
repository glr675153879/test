package com.hscloud.hs.cost.account.model.vo.kpi;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

/**
 * @author Administrator
 */
@Data
@Schema(description = "转科数据明细")
public class KpiTransferInfoVO2 {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "周期")
    private String period;

    private String userName;

    @Schema(description = "核算项名称")
    private String itemName;

    @Schema(description = "核算项code")
    private String itemCode;

    @Schema(description = "数据发生科室")
    private String sourceDept;

    @Schema(description = "数值")
    private BigDecimal value;

    @Schema(description = "归集科室id")
    private Long deptId;

    @Schema(description = "归集科室名称")
    private String deptName;

    @Schema(description = "对应科室单元")
    private String deptName2;

    @Schema(description = "业务唯一id")
    private String busiCode;

    @Schema(description = "挂号科别")
    private String ghkb;

    public static KpiTransferInfoVO2 changeToVO(KpiItemResult itemResult) {
        KpiTransferInfoVO2 vo = new KpiTransferInfoVO2();
        BeanUtils.copyProperties(itemResult, vo);
        return vo;
    }
}
