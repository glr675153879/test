package com.hscloud.hs.cost.account.model.dto.kpi.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Classname KeyValueDTO
 * @Description TODO
 * @Date 2024-01-04 20:37
 * @Created by sch
 */
@Data
@Accessors(chain = true)
@HeadRowHeight(value = 20)
@ColumnWidth(value = 25)
public class ExcelKeyUserFactorImportDto {

    @Schema(description = "用户id")
    @ExcelProperty(value = "用户id",index = 0)
    private Long userId;

    @Schema(description = "职务")
    @ExcelProperty(value = "职务", index = 1)
    private String office;

    @Schema(description = "职称")
    @ExcelProperty(value = "职称", index = 2)
    private String jobTitle;

    @Schema(description = "绩效岗位")
    @ExcelProperty(value = "绩效岗位", index = 3)
    private String performancePositions;

    @Schema(description = "事业单位岗位")
    @ExcelProperty(value = "事业单位岗位", index = 4)
    private String publicPositions;

    @Schema(description = "用工性质")
    @ExcelProperty(value = "用工性质", index = 5)
    private String employmentNature;

    @Schema(description = "人员类别")
    @ExcelProperty(value = "人员类别", index = 6)
    private String personnelCategory;
}
