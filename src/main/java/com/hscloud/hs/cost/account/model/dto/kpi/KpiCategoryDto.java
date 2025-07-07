package com.hscloud.hs.cost.account.model.dto.kpi;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Classname SysCategoryInput
 * @Description TODO
 * @Date 2024-09-09 16:13
 * @Created by sch
 */
@Data
public class KpiCategoryDto {

    @Schema(description = "分类ID")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "分类类型  user_group 人员分组  item_group  核算项分组  index_group 指标分组 allocat_group 分摊指标分组 imputation_type 归集分组")
    @JsonProperty("category_type")
    private String categoryType;

    @Schema(description = "代码")
    private String categoryCode;

    @Schema(description = "三方编码")
    private String thirdCode;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "parentId")
    private Long parentId;

    @Schema(description = "排序号")
    @JsonProperty("seq")
    private Long seq;

    @Schema(description = "0启用 1停用")
    @JsonProperty("status")
    private Long status;


    @Schema(description = "方案分组")
    private String planType;

    @Schema(description = "业务类型，1，一次绩效，2，科室成本")
    private String busiType = "1";

}
