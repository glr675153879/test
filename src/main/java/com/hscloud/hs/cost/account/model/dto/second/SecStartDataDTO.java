package com.hscloud.hs.cost.account.model.dto.second;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.model.entity.second.ProgDetailItem;
import com.hscloud.hs.cost.account.model.entity.second.ProgProject;
import com.hscloud.hs.cost.account.model.entity.second.ProgProjectDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Schema(description = "二次分配下发基础数据")
public class SecStartDataDTO {

    @Schema(description = "考勤")
    private List<Attendance> attendanceAll;

    @Schema(description = "project")
    private List<ProgProject> progProjectAll;

    @Schema(description = "detail")
    private List<ProgProjectDetail> progProjectDetailAll;

    @Schema(description = "item")
    private List<ProgDetailItem> progDetailItemAll;



}
