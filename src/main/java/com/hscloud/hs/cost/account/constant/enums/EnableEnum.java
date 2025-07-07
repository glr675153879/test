package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * 停启用
 * @author  lian
 * @date  2024/5/30 16:18
 *
 */
@Getter
public enum EnableEnum {

	/**
	 *
	 */
	ENABLE("0", "启用"),

	DISABLE("1", "停用")

	;

	private String type;

	private String desc;

	EnableEnum(String type, String desc) {
		this.type = type;
		this.desc = desc;
	}

	public static EnableEnum getByCode(String code) {
		for (EnableEnum en : EnableEnum.values()) {
			if (en.getType().equals(code)) {
				return en;
			}
		}
		return null;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

}
