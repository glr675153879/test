package com.hscloud.hs.cost.account.model.dto.dataReport;

import com.hscloud.hs.cost.account.constant.enums.dataReport.AccountUnitTypeEnum;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostClusterUnit;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportDetailCost;
import lombok.Data;

import javax.persistence.Entity;
import java.util.List;

@Data
@Entity
public class CostReportDetailInfoDto {
        private Long id;
        private Long recordId;
        private String measureGroup;
        private String jobNumber;
        private List<CostReportDetailCost> costList;
        private String isRemoved;
        private SysUserDto user;
        private List<MeasureDto> measureUnit;
        private List<CostClusterUnit> clusterUnits;
        private String userInfo;
        private String measureUnitInfo;
        private String clusterUnitsInfo;
        private String deptType;

        // 备注信息
        private String note;

        /**
         * 核算单元类型
         * {@link AccountUnitTypeEnum}
         */
        private String accountingUnitType;
}
