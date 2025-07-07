package com.hscloud.hs.cost.account.model.dto.deptCost;

import com.amazonaws.services.s3.transfer.Copy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @author banana
 * @create 2024-09-24 18:48
 */
@Data
public class DcSysSoftWareOrDeptAreaValueDto {

    /**
     * 周期（type = NOCOPY）
     */
    private String dt;

    /**
     * 核算单元id（type = NOCOPY）
     */
    private String accountUnitId;

    /**
     * 任务id （type = COPY）
     */
    private Long taskId;

    /**
     * 类型
     */
    private Type type;


    @Getter
    @AllArgsConstructor
    public enum Type {

        NOCOPY("0", "非备份表数据"),
        COPY("1", "备份表数据");

        private String val;

        private String desc;
    }
}
