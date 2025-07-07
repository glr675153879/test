package com.hscloud.hs.cost.account.constant.enums.imputation;

import lombok.Getter;

/**
 * 是否
 * @author  lian
 * @date  2024/5/30 16:18
 *
 */
@Getter
public enum YesNoEnum {

	Y("Y", "是"),
	N("N", "否"),
	DEFAULT("", "");

	private String type;

	private String desc;

	YesNoEnum(String type, String desc) {
		this.type = type;
		this.desc = desc;
	}

	public static YesNoEnum getByCode(String code) {
		if (code == null || code.isEmpty()) {
			return YesNoEnum.DEFAULT;
		}
		for (YesNoEnum en : YesNoEnum.values()) {
			if (en.getType().equals(code)) {
				return en;
			}
		}
		return YesNoEnum.DEFAULT;
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
