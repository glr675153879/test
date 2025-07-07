package com.hscloud.hs.cost.account.model.vo.imputation;

import com.hscloud.hs.cost.account.model.entity.imputation.Imputation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/24 14:37
 */
@Data
@Schema(description = "历史记录")
public class HistoryVO {
    @Schema(description = "历史记录时间")
    private String cycle;

    public static HistoryVO build(Imputation imputation) {
        HistoryVO historyVO = new HistoryVO();
        historyVO.setCycle(imputation.getImputationCycle());
        return historyVO;
    }
}
