package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * 通用分组类型
 */
@Getter
public enum GroupType {
	secondItem("二次分配核算项"),
	secondProg("二次分配方案");

	private final String name;
	private GroupType(String name){
		this.name = name;
	}

	public static GroupType getByCode(String code) {
		for (GroupType en : GroupType.values()) {
			if (en.toString().equals(code)) {
				return en;
			}
		}
		return null;
	}
}
