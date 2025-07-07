package com.hscloud.hs.cost.account.model.dto.imputation;

import com.baomidou.mybatisplus.annotation.TableField;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationDeptUnit;
import com.hscloud.hs.cost.account.model.vo.imputation.ImputationDetailsVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author xiechenyu
 * @Description：
 * @date 2024/4/22 15:26
 */
@Data
@Schema(description = "归集管理匹配，编辑入参")
public class ImputationDeptUnitDTO extends ImputationDeptUnit {

    @Schema(description = "科室人员校验，科室：DEPT，人员：USER")
    private String keyType;

    @Schema(description = "人员明细")
    List<ImputationDetailsVO> imputationDetails;


    @Schema(description = "人员信息")
    private Map<String, Object> leaderUser;

    @Schema(description = "科室信息")
    private Map<String, Object> leaderDept;

    @Schema(description = "原有人员明细")
    List<ImputationDetailsVO> imputationDetailsVOs;
}
