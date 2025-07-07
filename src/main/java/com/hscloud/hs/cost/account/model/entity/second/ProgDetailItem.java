package com.hscloud.hs.cost.account.model.entity.second;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hf
 * @since 2023-10-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "方案科室二次分配明细大项")
@TableName("sec_prog_detail_item")
public class ProgDetailItem extends BaseEntity<ProgDetailItem> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "核算指标明细id")
    @Schema(description = "核算指标明细id")
    private Long progProjectDetailId;

    @Column(comment = "项目名称")
    @Schema(description = "项目名称")
    private String name;

    @Column(comment = "父id 根节点 null")
    @Schema(description = "父id 根节点 null")
    private Long parentId;

    @TableField(exist = false)
    List<ProgDetailItem> childItemList = new ArrayList<>();

    @TableField(exist = false)
    @Schema(description = "操作标记")
    private String actionType;

    //工作量
    @Column(comment = "分配方式、计算方式")
    @Schema(description = "分配方式、计算方式")
    private String modeType;

    @Column(comment = "每数量单位标准",decimalLength = 6,length = 15)
    @Schema(description = "每数量单位标准")
    private BigDecimal priceValue;

    @Column(comment = "数据来源 字典")
    @Schema(description = "数据来源 字典")
    private String inputType;

    @Column(comment = "核算项id")
    @Schema(description = "核算项id")
    private String accountItemId;

    @Column(comment = "核算项code")
    @Schema(description = "核算项code")
    private String accountItemCode;

    @Column(comment = "核算项名称")
    @Schema(description = "核算项名称")
    private String accountItemName;

    @Column(comment = "核算项type 1核算项 2核算指标")
    @Schema(description = "核算项type 1核算项 2核算指标")
    private String accountItemType;

    @Column(comment = "是否继承上月数量")
    @Schema(description = "是否继承上月数量")
    private String ifExtendLast;

    @Column(comment = "排序号")
    @Schema(description = "排序号")
    private Long sortNum;

    //冗余字段
    @Column(comment = "模板方案id")
    @Schema(description = "模板方案id")
    private Long progCommonId;

    //冗余字段
    @Column(comment = "模板id")
    @Schema(description = "模板id")
    private Long commonId;

}
