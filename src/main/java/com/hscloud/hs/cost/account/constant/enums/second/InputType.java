package com.hscloud.hs.cost.account.constant.enums.second;

import lombok.Getter;

/**
 * 数据采集方式
 */
@Getter
public enum InputType {
	input("手工上报"),
	auto("系统采集");

	private final String name;
	private InputType(String name){
		this.name = name;
	}

	public static InputType getByCode(String code) {
		for (InputType en : InputType.values()) {
			if (en.toString().equals(code)) {
				return en;
			}
		}
		return null;
	}
}
