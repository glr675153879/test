package com.hscloud.hs.cost.account.model.vo.imputation;

import com.hscloud.hs.cost.account.model.entity.imputation.ImputationDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/22 16:30
 */
@Data
public class ImputationDetailsVO extends ImputationDetails {

    @Schema(description = "人员信息")
    Map<String, Object> leaderUser;
}
