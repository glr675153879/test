package com.hscloud.hs.cost.account.model.dto.dataReport;

import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportRecordFileInfo;
import lombok.Data;

import javax.persistence.Entity;
import java.util.List;

@Data
@Entity
public class CostReportDetailRecordDto {

    private List<CostReportDetailInfoDto> costReportDetailInfo;
    private List<CostReportRecordFileInfo> fileInfos;
}
