package com.hscloud.hs.cost.account.model.dto.second;

import com.alibaba.fastjson.JSONObject;
import com.hscloud.hs.cost.account.model.entity.second.ProgProject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "方案发布")
public class ProgrammePublishDTO {

    @Schema(description = "programmeId")
    private Long programmeId;

    @Schema(description = "project及以下的数据组装，每条数据带有增删改标记")
    private List<ProgProject> projectList;

}
