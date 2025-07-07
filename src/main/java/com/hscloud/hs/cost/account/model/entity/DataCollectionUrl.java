package com.hscloud.hs.cost.account.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author banana
 * @create 2023-09-21 19:43
 */

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "数据采集映射表")
public class DataCollectionUrl extends Model<DataCollectionUrl> {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "code")
    private String code;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "接口地址")
    private String url;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用key，平台分配")
    private String appKey;

    @Schema(description = "密钥，平台分配")
    private String appSecret;

    @Schema(description = "加密方式 1.MD5 2.SHA256 3.SM3")
    private String signEncryptType;

    @Schema(description = "租户id")
    private Long tenantId;
}
