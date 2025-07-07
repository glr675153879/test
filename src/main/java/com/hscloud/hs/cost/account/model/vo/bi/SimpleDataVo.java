package com.hscloud.hs.cost.account.model.vo.bi;

import cn.hutool.core.lang.tree.Tree;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 11:30]
 */
@Data
@Schema(description = "简单表格数据")
public class SimpleDataVo {

    @Schema(description = "单元格数据")
    List<Map<String, Object>> rows;


    @Schema(description = "表头")
    List<Tree<Long>> heads;

}
