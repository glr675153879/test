package com.hscloud.hs.cost.account.model.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "二次分配人员明细详情对象")
public class SecondUserDistributionDetail {

    @Schema(description = "工号")
    private Long jobNumber;


    @Schema(description = "姓名")
    private String name;



    @Schema(description = "单个")
    private List<SecondUserDistributionUnitSingle> secondUserDistributionUnitSingleList;

    @Schema(description = "通用单元绩效")
    private List<SecondUserDistributionUnitMulti> secondUserDistributionUnitMultiList;


}
