package com.hscloud.hs.cost.account.constant;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * 数据小组获取是否收费记录
 * @author  lian
 * @date  2024/6/7 9:03
 *
 */

@Getter
public enum ResourceIsChargeEnum {

	CHARGED("CHARGED", "可收费","Y"),
	NOT_CHARGED("NOT_CHARGED", "不可收费","N"),
	NOT_SET("NOT_SET", "未设置",null),
	DEFAULT("", "","");


	private String code;

	private String type;

	private String desc;

	ResourceIsChargeEnum(String code, String desc,String type) {
		this.code = code;
		this.desc = desc;
		this.type = type;
	}

	public static ResourceIsChargeEnum getByCode(String code) {
		if (code == null || code.isEmpty()) {
			return ResourceIsChargeEnum.DEFAULT;
		}
		for (ResourceIsChargeEnum en : ResourceIsChargeEnum.values()) {
			if (en.getCode().equals(code)) {
				return en;
			}
		}
		return ResourceIsChargeEnum.DEFAULT;
	}

	public static ResourceIsChargeEnum getByDesc(String desc) {
		if (StrUtil.isBlank(desc)) {
			return ResourceIsChargeEnum.DEFAULT;
		}
		for (ResourceIsChargeEnum en : ResourceIsChargeEnum.values()) {
			if (en.getDesc().equals(desc)) {
				return en;
			}
		}
		return ResourceIsChargeEnum.DEFAULT;
	}


	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
