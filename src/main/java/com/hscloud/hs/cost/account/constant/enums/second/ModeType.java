package com.hscloud.hs.cost.account.constant.enums.second;

import lombok.Getter;

/**
 * 科室二次分配 分配方式
 * 单项绩效 计算方式
 */
@Getter
public enum ModeType {
	ratio("系数分配"),
	work("工作量分配"),
	qtyxprice("按数量*标准计算方式"),
	input("直接输入奖金方式");

	private final String name;
	private ModeType(String name){
		this.name = name;
	}

	public static ModeType getByCode(String code) {
		for (ModeType en : ModeType.values()) {
			if (en.toString().equals(code)) {
				return en;
			}
		}
		return null;
	}
}
