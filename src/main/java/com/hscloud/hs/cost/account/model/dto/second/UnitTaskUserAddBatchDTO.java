package com.hscloud.hs.cost.account.model.dto.second;

import com.alibaba.fastjson.JSONObject;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "人员批量新增")
public class UnitTaskUserAddBatchDTO {

    @Schema(description = "unitTaskId")
    private Long unitTaskId;

    @Schema(description = "userList")
    private List<Attendance> userList;

}
