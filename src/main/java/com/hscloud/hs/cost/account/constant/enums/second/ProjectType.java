package com.hscloud.hs.cost.account.constant.enums.second;

import lombok.Getter;

/**
 * 绩效类型
 */
@Getter
public enum ProjectType {
	zhuanxiang("专项绩效"),
	danxiang("单项绩效"),
	pinjun("平均绩效"),
	erci("科室二次分配");

	private final String name;
	private ProjectType(String name){
		this.name = name;
	}

	public static ProjectType getByCode(String code) {
		for (ProjectType en : ProjectType.values()) {
			if (en.toString().equals(code)) {
				return en;
			}
		}
		return null;
	}
}
