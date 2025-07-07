package com.hscloud.hs.cost.account.constant.enums.dataReport;

import lombok.Getter;

/**
 * 上报频率
 */
@Getter
public enum FrequencyType {
	month("月份");

	private final String name;
	private FrequencyType(String name){
		this.name = name;
	}

	public static FrequencyType getByCode(String code) {
		for (FrequencyType en : FrequencyType.values()) {
			if (en.toString().equals(code)) {
				return en;
			}
		}
		return null;
	}
}
