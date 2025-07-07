package com.hscloud.hs.cost.account.model.vo;

import com.hscloud.hs.cost.account.model.pojo.SecondUserDistributionDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "二次分配明细详情对象")
public class SecondTaskDistributionDetailVo {

    @Schema(description = "分配方案明细")
    private SecondDistributionGetAccountPlanDetailsVo accountPlanDetailsVo;


    @Schema(description = "二次分配人员明细详情对象")
    private List<SecondUserDistributionDetail> secondUserDistributionDetailList;


    @Schema(description = "总金额")
    private BigDecimal totalAmount;


    @Schema(description = "配置信息")
    private Map<String, Map<String,BigDecimal>> configMap;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;


}
