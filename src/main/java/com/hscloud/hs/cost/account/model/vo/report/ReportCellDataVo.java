package com.hscloud.hs.cost.account.model.vo.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 11:30]
 */
@EqualsAndHashCode(callSuper = false)
@Data
@Schema(description = "单元格数据")
public class ReportCellDataVo implements Serializable {

    private static final long serialVersionUID = -1979500028738587222L;
    /**
     * key：fieldName
     */
    @Schema(description = "单元格数据")
    Map<String, Object> cellMap;


}
