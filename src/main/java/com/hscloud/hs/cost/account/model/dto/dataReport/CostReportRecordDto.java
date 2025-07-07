package com.hscloud.hs.cost.account.model.dto.dataReport;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportDetailCost;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportDetailInfo;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Schema(description = "我的上报dto")
public class CostReportRecordDto extends CostReportRecord{

    private List<CostReportDetailInfo> infoList;
}
