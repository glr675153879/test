package com.hscloud.hs.cost.account.model.entity.userAttendance;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 人员考勤表
 *
 * @JC
 * @since 2024-05-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("cost_user_attendance_custom_fields")
@Schema(description = "人员考勤自定义字段表")
public class CostUserAttendanceCustomFields extends BaseEntity<CostUserAttendanceCustomFields> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "核算周期")
    @Schema(description = "核算周期")
    private String dt;

    @Column(comment = "字段名")
    @Schema(description = "字段名")
    private String name;

    @Column(comment = "字段Id")
    @Schema(description = "字段Id")
    private String columnId;

    @Column(comment = "数据类型")
    @Schema(description = "数据类型")
    private String dataType;

    @Column(comment = "编码")
    @Schema(description = "编码")
    private String code;

    @Column(comment = "状态 1:启用 0:停用", defaultValue = "1")
    @Schema(description = "状态 1:启用 0:停用")
    private String status;

    @Column(comment = "排序")
    @Schema(description = "排序")
    private Integer sortNum;

    @Column(comment = "是否必填 1：必填 0：非必填", defaultValue = "0")
    @Schema(description = "是否必填 1：必填 0：非必填")
    private String requireFlag;

    @Column(comment = "字段类型 01：数值 02：文本 03：下拉 04：整数")
    @Schema(description = "字段类型 01：数值 02：文本 03：下拉 04：整数")
    private String fieldType;

    @Column(comment = "校验类型 0100:无 | 0101:小于自然月天数大于等于0 | 0102:正数 | 0103:0到1之间 | 0400:无 | 0401:正整数 | 0402:非负整数 | 0403:负整数 | 0404:非正整数")
    @Schema(description = "校验类型 0100:无 | 0101:小于自然月天数大于等于0 | 0102:正数 | 0103:0到1之间 | 0400:无 | 0401:正整数 | 0402:非负整数 | 0403:负整数 | 0404:非正整数")
    private String fieldCheck;


}
