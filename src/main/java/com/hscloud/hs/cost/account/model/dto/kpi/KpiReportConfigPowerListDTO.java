package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.TableField;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportConfigPower;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class KpiReportConfigPowerListDTO extends KpiReportConfigPower {

    @Schema(description = "人员Name")
    @Column(comment = "人员Name")
    private String userName ;
    @Schema(description = "分组")
    @Column(comment = "分组")
    private String groupName ;
    @Schema(description = "科室Name")
    @Column(comment = "科室Name")
    private String deptName ;
    @Schema(description = "报表Name")
    @Column(comment = "报表Name")
    private String reportName ;
}
