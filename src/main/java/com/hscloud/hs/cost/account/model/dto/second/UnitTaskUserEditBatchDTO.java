package com.hscloud.hs.cost.account.model.dto.second;

import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "人员批量修改")
public class UnitTaskUserEditBatchDTO {

    @Schema(description = "userList")
    private List<UnitTaskUser> userList;

}
