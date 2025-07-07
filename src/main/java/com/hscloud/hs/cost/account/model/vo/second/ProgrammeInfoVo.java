package com.hscloud.hs.cost.account.model.vo.second;

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
@Schema(description = "任务核算指标明细值")
public class ProgrammeInfoVo extends Programme {

    @Schema(description = "project及以下的数据组装，每条数据带有增删改标记")
    private List<ProgProject> projectList;
}
