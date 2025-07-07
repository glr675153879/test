package com.hscloud.hs.cost.account.model.entity.dataReport;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "ods_his_unidrgswed_dbo_drgs_info")
public class OdsHisUnidrgswedDboDrgsInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id // 假设你有一个主键，这里只是示例，你可能需要添加实际的主键注解和字段
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dt", length = 6)
    private String dt;

    @Column(name = "ward_name", length = 32)
    private String wardName;

    @Column(name = "ord_code", length = 20)
    private String ordCode;

    @Column(name = "reg_code", length = 20)
    private String regCode;

    @Column(name = "inhos_time")
    private Date inhosTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "outhos_time")
    private Date outhosTime;

    @Column(name = "outhos_status", length = 10)
    private String outhosStatus;

    @Column(name = "person_name", length = 255)
    private String personName;

    @Column(name = "sex", length = 255)
    private String sex;

    @Column(name = "age")
    private Integer age;

    @Column(name = "out_dept_name", length = 255)
    private String outDeptName;

    @Column(name = "zyts")
    private Integer zyts;

    @Column(name = "drg_code", length = 20)
    private String drgCode;

    @Column(name = "drg_name", length = 300)
    private String drgName;

    @Column(name = "disease_name", columnDefinition = "MEDIUMTEXT")
    private String diseaseName;

    @Column(name = "rw", precision = 16, scale = 6)
    private BigDecimal rw;

    @Column(name = "hight_disease")
    private Integer hightDisease;

    @Column(name = "major_code", length = 20)
    private String majorCode;

    @Column(name = "major_name", length = 200)
    private String majorName;

    @Column(name = "qtzd1_code", length = 20)
    private String qtzd1Code;

    @Column(name = "qtzd1_name", length = 200)
    private String qtzd1Name;

    @Column(name = "qtzd2_code", length = 20)
    private String qtzd2Code;

    @Column(name = "qtzd2_name", length = 200)
    private String qtzd2Name;

    @Column(name = "qtzd3_code", length = 20)
    private String qtzd3Code;

    @Column(name = "qtzd3_name", length = 200)
    private String qtzd3Name;

    @Column(name = "qtzd4_code", length = 20)
    private String qtzd4Code;

    @Column(name = "qtzd4_name", length = 200)
    private String qtzd4Name;

    @Column(name = "qtzd5_code", length = 20)
    private String qtzd5Code;

    @Column(name = "qtzd5_name", length = 200)
    private String qtzd5Name;

    @Column(name = "qtzd6_code", length = 20)
    private String qtzd6Code;

    @Column(name = "qtzd6_name", length = 200)
    private String qtzd6Name;

    @Column(name = "qtzd7_code", length = 20)
    private String qtzd7Code;

    @Column(name = "qtzd7_name", length = 200)
    private String qtzd7Name;

    @Column(name = "qtzd8_code", length = 20)
    private String qtzd8Code;

    @Column(name = "qtzd8_name", length = 200)
    private String qtzd8Name;

    @Column(name = "qtzd9_code", length = 20)
    private String qtzd9Code;

    @Column(name = "qtzd9_name", length = 200)
    private String qtzd9Name;

    @Column(name = "qtzd10_code", length = 20)
    private String qtzd10Code;

    @Column(name = "qtzd10_name", length = 200)
    private String qtzd10Name;

    @Column(name = "first_code", length = 20)
    private String firstCode;

    @Column(name = "first_name", length = 200)
    private String firstName;

    @Column(name = "second_code", length = 20)
    private String secondCode;

    @Column(name = "second_name", length = 200)
    private String secondName;

    @Column(name = "third_code", length = 20)
    private String thirdCode;

    @Column(name = "third_name", length = 200)
    private String thirdName;

    @Column(name = "fourth_code", length = 20)
    private String fourthCode;

    @Column(name = "fourth_name", length = 200)
    private String fourthName;

    @Column(name = "fifth_code", length = 20)
    private String fifthCode;

    @Column(name = "fifth_name", length = 200)
    private String fifthName;

    @Column(name = "total_fee", precision = 16, scale = 6)
    private BigDecimal totalFee;

    @Column(name = "xy_fee", precision = 16, scale = 6)
    private BigDecimal xyFee; // 西药费

    @Column(name = "zy_fee", precision = 16, scale = 6)
    private BigDecimal zyFee; // 中药费

    @Column(name = "hc_fee", precision = 16, scale = 6)
    private BigDecimal hcFee; // 耗材费

    @Column(name = "kzr_name", length = 50)
    private String kzrName; // 科主任名称

    @Column(name = "kzr_code")
    private Integer kzrCode; // 科主任编码

    @Column(name = "zr_name", length = 50)
    private String zrName; // 主任名称

    @Column(name = "zr_code")
    private Integer zrCode; // 主任编码

    @Column(name = "zz_name", length = 50)
    private String zzName; // 主治名称

    @Column(name = "zz_code")
    private Integer zzCode; // 主治编码

    @Column(name = "zy_name", length = 50)
    private String zyName; // 住院医师名称

    @Column(name = "zy_code")
    private Integer zyCode; // 住院医师编码

    @Column(name = "zk_name", length = 100)
    private String zkName; // 质控名称

    @Column(name = "zk_code")
    private Integer zkCode; // 质控编码

    @Column(name = "account_id", length = 64)
    private String accountId; // 核算单元名称

    @Column(name = "account_unit", length = 64)
    private String accountUnit; // 核算单元名称

    @Column(name = "is_editable", length = 64)
    private Boolean isEditable; //  0 可被编辑 1 不可被编辑

    @Schema(description = "业务类型: 0绩效 1科室成本")
    private String type = "0";

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
