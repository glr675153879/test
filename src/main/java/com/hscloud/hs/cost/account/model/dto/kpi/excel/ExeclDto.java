package com.hscloud.hs.cost.account.model.dto.kpi.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Classname ExeclDto
 * @Description TODO
 * @Date 2023-09-29 16:51
 * @Created by sch
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExeclDto {

    private List<List<String>> datas;

    private String sheetName;

    private Integer sheetSno;
}
