package com.hscloud.hs.cost.account.model.dto.second;

import com.hscloud.hs.cost.account.model.entity.second.GrantUnit;
import com.hscloud.hs.cost.account.model.entity.second.ProgProject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "发放单元dto")
public class GrantUnitAddDTO extends GrantUnit {

    @Schema(description = "project及以下的数据组装，每条数据带有增删改标记")
    private List<ProgProject> projectList;

}
