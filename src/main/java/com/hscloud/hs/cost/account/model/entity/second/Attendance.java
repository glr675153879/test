package com.hscloud.hs.cost.account.model.entity.second;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.javers.core.metamodel.annotation.Entity;


/**
 * @author hf
 * @since 2023-10-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Schema(description = "考勤表")
@TableName("cost_attendance")
public class Attendance extends BaseEntity<Attendance> {

    private static final long serialVersionUID = 1L;

    @Column(comment = "基础平台用户id")
    @Schema(description = "基础平台用户id")
    private Long userId;

    @Column(comment = "基础平台部门id")
    @Schema(description = "基础平台部门id")
    private Long deptId;

    @Column(comment = "周期")
    @Schema(description = "周期")
    private String cycle;

    @Column(comment = "科室单元id")
    @Schema(description = "科室单元id")
    private String accountUnitId;


    @Column(comment = "科室单元名称")
    @Schema(description = "科室单元名称")
    private String accountUnitName;

    @Column(comment = "工号")
    @Schema(description = "工号")
    private String empCode;

    @Column(comment = "姓名")
    @Schema(description = "姓名")
    private String empName;

    @Column(comment = "职称")
    @Schema(description = "职称")
    private String title;

    @Column(comment = "科室名称")
    @Schema(description = "科室名称")
    private String deptName;


    @Column(comment = "人员组别")
    @Schema(description = "人员组别")
    private String groupName;

    @Column(comment = "出勤系数")
    @Schema(description = "出勤系数")
    private String workRate;

    @Column(comment = "上报考勤组所在天数")
    @Schema(description = "上报考勤组所在天数")
    private String groupWorkdays;

    @Column(comment = "在册系数")
    @Schema(description = "在册系数")
    private String zaiceRate;

    @Column(comment = "员工工作性质")
    @Schema(description = "员工工作性质")
    private String workType;

    @Column(comment = "出勤天数（整理）")
    @Schema(description = "出勤天数（整理）")
    private String workdayszl;

    @Column(comment = "出勤天数")
    @Schema(description = "出勤天数")
    private String workdays;

    @Column(comment = "是否拿奖金")
    @Schema(description = "是否拿奖金")
    private String ifGetAmt;

    @Column(comment = "自定义字段", type = MySqlTypeConstant.TEXT)
    @Schema(description = "自定义字段")
    private String customFields;

    @Column(comment = "发热门诊")
    @Schema(description = "发热门诊")
    private String faremz;

    @Column(comment = "进修")
    @Schema(description = "进修")
    private String jinxiu;

    @Column(comment = "外院轮转")
    @Schema(description = "外院轮转")
    private String waiyuanlz;

    @Column(comment = "下乡")
    @Schema(description = "下乡")
    private String xiaxiang;

    @Column(comment = "下沉")
    @Schema(description = "下沉")
    private String xiachen;

    @Column(comment = "120急救中心")
    @Schema(description = "120急救中心")
    private String jiujizx;


    @Column(comment = "上级借调")
    @Schema(description = "上级借调")
    private String shangjijd;

    @Column(comment = "外派驻村")
    @Schema(description = "外派驻村")
    private String waipaizc;

    @Column(comment = "外派支援")
    @Schema(description = "外派支援")
    private String waipaizy;

    @Column(comment = "中治室")
    @Schema(description = "中治室")
    private String zhongzhis;

    @Column(comment = "热疗")
    @Schema(description = "热疗")
    private String reliao;

    @Column(comment = "离岗退养")
    @Schema(description = "离岗退养")
    private String ligangty;

    @Column(comment = "工伤")
    @Schema(description = "工伤")
    private String gongshang;

    @Column(comment = "产假")
    @Schema(description = "产假")
    private String chanjia;

    @Column(comment = "病假")
    @Schema(description = "病假")
    private String bingjia;

    @Column(comment = "外出学习(人才培养)")
    @Schema(description = "外出学习(人才培养)")
    private String waichuxx;

    @TableField(exist = false)
    @Schema(description = "排序号")
    private Integer sortNum;

}
