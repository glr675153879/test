package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.*;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "浙江省基本医疗保险医疗服务项目目录")
@TableName("kpi_service_item")
public class KpiServiceItem extends Model<KpiServiceItem> {
    @IsAutoIncrement
    @TableId(value = "id", type = IdType.AUTO)
    @Column(comment = "本地记录唯一号", type = MySqlTypeConstant.BIGINT)
    private Long id;

    @TableField(value = "list_type")
    @Column(comment = "目录类别（101西药;102中成药;103中药饮片;105民族药品;201服务项目;301医用材料）", type = MySqlTypeConstant.VARCHAR)
    private String listType;

    @TableField(value = "type")
    @Column(comment = "目录类别类型（1药品，2服务，3材料）", type = MySqlTypeConstant.VARCHAR)
    private String type;

    @TableField(value = "item_code")
    @Column(comment = "医疗目录编码", type = MySqlTypeConstant.VARCHAR, length = 100)
    private String itemCode;

    @TableField(value = "begin_date")
    @Column(comment = "开始日期", type = MySqlTypeConstant.DATETIME)
    private Date beginDate;

    @TableField(value = "end_date")
    @Column(comment = "结束日期", type = MySqlTypeConstant.DATETIME)
    private Date endDate;

    @TableField(value = "reg_appr_no")
    @Column(comment = "批准文号", type = MySqlTypeConstant.VARCHAR)
    private String regApprNo;

    @TableField(value = "form_id")
    @Column(comment = "剂型", type = MySqlTypeConstant.VARCHAR, length = 300)
    private String formId;

    @TableField(value = "excluded_content")
    @Column(comment = "除外内容", type = MySqlTypeConstant.VARCHAR, length = 2000)
    private String excludedContent;

    @TableField(value = "connotation")
    @Column(comment = "项目内涵", type = MySqlTypeConstant.VARCHAR, length = 2000)
    private String connotation;

    @TableField(value = "unit")
    @Column(comment = "计价单位", type = MySqlTypeConstant.VARCHAR)
    private String unit;

    @TableField(value = "spec")
    @Column(comment = "规格", type = MySqlTypeConstant.VARCHAR, length = 512)
    private String spec;

    @TableField(value = "pack_spec")
    @Column(comment = "包装规格", type = MySqlTypeConstant.VARCHAR)
    private String packSpec;

    @TableField(value = "remark")
    @Column(comment = "备注", type = MySqlTypeConstant.VARCHAR, length = 1500)
    private String remark;

    @TableField(value = "medins_item_code")
    @Column(comment = "定点医药机构目录编号", type = MySqlTypeConstant.VARCHAR)
    private String medinsItemCode;

    @TableField(value = "medins_item_name")
    @Column(comment = "定点医药机构目录名称", type = MySqlTypeConstant.VARCHAR)
    private String medinsItemName;

    @TableField(value = "product_factory_name")
    @Column(comment = "生产厂家", type = MySqlTypeConstant.VARCHAR)
    private String productFactoryName;

    @TableField(value = "item_content")
    @Column(comment = "提醒内容", type = MySqlTypeConstant.TEXT)
    private String itemContent;

    @TableField(value = "insure_class")
    @Column(comment = "医保分类", type = MySqlTypeConstant.VARCHAR, length = 20)
    private String insureClass;

    @TableField(value = "item_level")
    @Column(comment = "收费项目等级", type = MySqlTypeConstant.VARCHAR, length = 30)
    private String itemLevel;

    @TableField(value = "limit_used_flag")
    @Column(comment = "限制使用标志", type = MySqlTypeConstant.VARCHAR, length = 32)
    private String limitUsedFlag;

    @TableField(value = "province_code")
    @Column(comment = "省编码", type = MySqlTypeConstant.VARCHAR)
    private String provinceCode;

    @TableField(value = "country_code")
    @Column(comment = "国家编码", type = MySqlTypeConstant.VARCHAR)
    private String countryCode;
}
