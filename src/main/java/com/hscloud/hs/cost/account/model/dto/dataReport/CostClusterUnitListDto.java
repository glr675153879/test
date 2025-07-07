package com.hscloud.hs.cost.account.model.dto.dataReport;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostClusterUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.Entity;
import java.util.List;

@Data
@Entity
public class CostClusterUnitListDto {
        private Long id;
        private String name;
        private String unitList;
        private String isFixUnit;
        private String status;
        private String initialized;
        private List<CostClusterUnit> costClusterUnitList;
    }
