package com.hscloud.hs.cost.account.model.vo.second.count;

import cn.hutool.core.lang.tree.Tree;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author 小小w
 * @date 2024/3/5 15:31
 */
@Data
@Schema(description = "绩效明细")
public class PerformanceDetailsVO implements Serializable {

    private static final long serialVersionUID = -71693290035575692L;

    @Schema(description = "表头 {id:\"1\", name:\"1\"}")
    private List<Tree<String>> heads;

    private List<Map<String, Object>> data;
}
