package com.hscloud.hs.cost.account.constant.enums.second;

import lombok.Getter;

/**
 * 进位规则
 */
@Getter
public enum CarryRule {
	halfup("四舍五入"),
	down("向下取整"),
	up("向上取整");

	private final String name;
	private CarryRule(String name){
		this.name = name;
	}

	public static CarryRule getByCode(String code) {
		for (CarryRule en : CarryRule.values()) {
			if (en.toString().equals(code)) {
				return en;
			}
		}
		return null;
	}
}
