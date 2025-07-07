package com.hscloud.hs.cost.account.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "异动记录Vo")
public class CostDataChangeRecordCountVo {

    @Schema(description = "异动总数")
    private Long changeRecordCount;

    @Schema(description = "当前业务对应的数量")
    private List<BizObject> bizObjectList;

    @Data
    @Schema(description = "业务对象异动Vo")
    public static class BizObject {
        @Schema(description = "业务id")
        private Long bizId;

        @Schema(description = "业务异动数量")
        private Long bizCount;
    }

}
