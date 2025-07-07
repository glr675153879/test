package com.hscloud.hs.cost.account.model.vo.second;

import com.hscloud.hs.cost.account.model.entity.second.UnitTaskDetailItem;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskDetailItemWork;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "任务科室二次分配明细大项值")
public class UnitTaskDetailItemVo extends UnitTaskUser {

    private static final long serialVersionUID = 6909052771008839152L;

    @Schema(description = "任务科室二次分配明细大项值")
    private List<UnitTaskDetailItem> itemList;

    @Schema(description = "工作量数据id")
    private UnitTaskDetailItemWork itemWork;

}
