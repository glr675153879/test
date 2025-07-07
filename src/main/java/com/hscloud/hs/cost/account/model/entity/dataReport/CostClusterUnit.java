package com.hscloud.hs.cost.account.model.entity.dataReport;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "归集单元")
@Entity
@Table(name = "cost_cluster_unit")
public class CostClusterUnit extends BaseEntity<CostClusterUnit> {
    private static final long serialVersionUID = 1L;

    @Schema(description = "归集单元名称")
    private String name;

    @Schema(description = "分摊科室名称")
    private String units;

    @Schema(description = "固定科室单元标识 0 否 1是")
    private String isFixUnit;

    @Schema(description = "启停用标记，0启用，1停用")
    private String status;

    @Schema(description = "初始化状态，Y已初始化，N未初始化")
    private String initialized;

    @Schema(description = "第三方核算单元")
    private Long thirdAccountId;

    @Schema(description = "第三方应用id")
    private Long thirdId;

    @Schema(description = "第三方应用名称")
    private String thirdName;

    @Column(comment = "业务类型: 0绩效 1科室成本", length = 1,
            type = MySqlTypeConstant.VARCHAR, defaultValue = "0")
    @Schema(description = "业务类型: 0绩效 1科室成本")
    private String type = "0";

    @TableField(exist = false)
    @Schema(description = "上报项列表")
    private List<CostAccountUnit> unitList;

    @TableField(exist = false)
    @Schema(description = "操作人")
    private String operationName;

    @TableField(exist = false)
    @Schema(description = "操作时间")
    private LocalDateTime operationTime;
}
