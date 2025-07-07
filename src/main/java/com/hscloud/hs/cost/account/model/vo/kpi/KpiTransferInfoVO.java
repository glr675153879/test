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
public class KpiTransferInfoVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "核算项名称")
    private String itemName;

    @Schema(description = "数据发生科室")
    private String sourceDept;

    @Schema(description = "数值")
    private BigDecimal value;

    @Schema(description = "科室id")
    private Long deptId;

    @Schema(description = "科室名称")
    private String deptName;

    public static KpiTransferInfoVO changeToVO(KpiItemResult itemResult) {
        KpiTransferInfoVO vo = new KpiTransferInfoVO();
        BeanUtils.copyProperties(itemResult, vo);
        return vo;
    }
}
