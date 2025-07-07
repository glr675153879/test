package com.hscloud.hs.cost.account.model.vo.second;

import com.hscloud.hs.cost.account.model.entity.second.GrantUnit;
import com.hscloud.hs.cost.account.model.entity.second.ProgProject;
import com.hscloud.hs.cost.account.model.entity.second.Programme;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author 小小w
 * @date 2024/3/5 15:31
 */
@Data
@Schema(description = "我负责的发放单元")
public class GrantUnitMineVo extends GrantUnit {

    @Schema(description = "发放单元的方案id 唯一")
    private Long programmeId;
}
