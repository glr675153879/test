package com.hscloud.hs.cost.account.model.entity.second;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.constant.enums.second.InputType;
import com.hscloud.hs.cost.account.constant.enums.second.ModeType;
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
@Schema(description = "方案核算指标明细")
@TableName("sec_prog_project_detail")
public class ProgProjectDetail extends BaseEntity<ProgProjectDetail> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "核算指标")
    @Schema(description = "核算指标id")
    private Long progProjectId;

    @Column(comment = "项目名称")
    @Schema(description = "项目名称")
    private String name;


    @Column(comment = "分配方式、计算方式")
    @Schema(description = "分配方式、计算方式")
    private String modeType;

    @Schema(description = "分配方式、计算方式名称")
    @TableField(exist = false)
    private String modeTypeName;

    @Column(comment = "排序号")
    @Schema(description = "排序号")
    private Long sortNum;



    //单项绩效
    @Column(comment = "每数量单位标准",decimalLength = 6,length = 15)
    @Schema(description = "每数量单位标准")
    private BigDecimal priceValue = BigDecimal.ONE;

    @Column(comment = "数据来源 字典")
    @Schema(description = "数据来源 字典")
    private String inputType;

    @Column(comment = "核算项id")
    @Schema(description = "核算项id")
    private Long accountItemId;

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


    //科室二次分配
    @Column(comment = "权重",decimalLength = 6,length = 15)
    @Schema(description = "权重")
    private BigDecimal erciRate = BigDecimal.ZERO;

    @Column(comment = "是否与出勤挂钩")
    @Schema(description = "是否与出勤挂钩")
    private String ifCareWorkdays;

    @Column(comment = "1子项相加 0子项相乘")
    @Schema(description = "1子项相加 0子项相乘")
    private String ifParentItemValueAdd;

    @Column(comment = "1父项相加 0父项相乘")
    @Schema(description = "1父项相加 0父项相乘")
    private String ifItemValueAdd;

    //冗余字段
    @Column(comment = "模板方案id")
    @Schema(description = "模板方案id")
    private Long progCommonId;

    //冗余字段
    @Column(comment = "模板id")
    @Schema(description = "模板id")
    private Long commonId;

    @TableField(exist = false)
    @Schema(description = "操作标记")
    private String actionType;

    @TableField(exist = false)
    private List<ProgDetailItem> progDetailItemList = new ArrayList<>();


}
