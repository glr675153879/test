package com.hscloud.hs.cost.account.model.vo.report;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 11:30]
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "表格数据")
public class ReportTableDataVo extends Page<ReportTableDataVo> {

    private static final long serialVersionUID = -1979500028738587222L;
    /**
     * fieldName为key
     */
    @Schema(description = "单元格数据")
    List<Map<String, Object>> rows;

    @Schema(description = "执行sql")
    private String sql;


}
