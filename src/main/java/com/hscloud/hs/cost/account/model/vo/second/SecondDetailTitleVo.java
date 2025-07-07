package com.hscloud.hs.cost.account.model.vo.second;

import com.hscloud.hs.cost.account.model.entity.second.ProgProject;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectCount;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Schema(description = "分配结果 明细按人汇总title")
public class SecondDetailTitleVo {

    @Schema(description = "project title")
    private List<ProgProject> progProjectList;
}
