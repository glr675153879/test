package com.hscloud.hs.cost.account.model.entity.kpi;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "医疗服务目录")
@TableName("kpi_service_item_category")
public class KpiServiceItemCategory extends Model<KpiServiceItemCategory> {
    @IsAutoIncrement
    @TableId(value = "id", type = IdType.AUTO)
    @Column(comment = "null", type = MySqlTypeConstant.BIGINT)
    private Long id;

    @TableField(value = "code")
    @Column(comment = "目录编码", type = MySqlTypeConstant.VARCHAR, length = 64)
    private String code;

    @TableField(value = "name")
    @Column(comment = "目录名称", type = MySqlTypeConstant.VARCHAR)
    private String name;

    @TableField(value = "description")
    @Column(comment = "描述", type = MySqlTypeConstant.VARCHAR, length = 1000)
    private String description;

    @TableField(value = "except_desc")
    @Column(comment = "除外内容", type = MySqlTypeConstant.VARCHAR)
    private String exceptDesc;

    @TableField(value = "unit")
    @Column(comment = "计价单位", type = MySqlTypeConstant.VARCHAR, length = 64)
    private String unit;

    @TableField(value = "remark")
    @Column(comment = "备注", type = MySqlTypeConstant.VARCHAR, length = 1000)
    private String remark;

    @TableField(value = "connotation")
    @Column(comment = "项目内涵", type = MySqlTypeConstant.VARCHAR, length = 1000)
    private String connotation;

    @TableField(value = "leaf")
    @Column(comment = "叶子节点", type = MySqlTypeConstant.INT)
    private Integer leaf;

    //子节点
    @TableField(exist = false)
    private List<KpiServiceItemCategory> children = new ArrayList<>();

}