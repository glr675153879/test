package com.hscloud.hs.cost.account.model.vo.imputation;

import com.hscloud.hs.cost.account.model.entity.imputation.ImputationDeptUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/22 18:18
 */
@Data
public class ImputationMatchDeptUnitVO  {
    @Schema(description = "人员明细列表")
    List<ImputationDetailsVO> imputationDetailsVOs;
}
