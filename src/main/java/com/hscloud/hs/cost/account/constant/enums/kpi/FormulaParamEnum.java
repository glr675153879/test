package com.hscloud.hs.cost.account.constant.enums.kpi;

public enum FormulaParamEnum {

    P_10("10","本人员"),
    P_12("12","人员类型 对应user_type"),
    P_11("11","自定义人员"),
    P_13("13","按归集"),
    P_14("14","人员分组"),
    P_15("15","工作性质"),
    P_16("16","本人员及负责科室"),
    P_17("17","本人员及所在科室"),
    P_18("18","本人员限定科室"),
    P_19("19","本人员归集人员"),
    P_100("100","所有人员"),
    P_20("20","本科室单元"),
    P_21("21","自定义科室"),
    P_22("22","核算类型 对应kpi_calculate_type"),
    P_23("23","核算分组"),
    P_24("24","本人员负责科室"),
    P_25("25","科室单元人员类型（字典对应user_type）"),
    P_26("26","本人员所在科室"),
    P_29("29","所有科室单元"),
    P_310("310","本摊入人"),
    P_319("319","所有摊入人"),
    P_320("320","本摊入科室单元"),
    P_329("329","所有摊入科室单元"),
    P_ITEM("item","核算项"),
    P_EQUIVALENT("itemEquivalent","当量"),
    P_INDEX("index","指标"),
    P_SYSTEM("system","系统指标"),
    P_ALLOCATION("allocation","分摊"),
    P_QYFT("qyft","全院分摊"),
    P_YHFT("yhft","医护分摊"),
    P_JCHBQ("jchbq","借床或病区分摊"),
    P_MZASR("mzasr","门诊共用按收入分摊"),
    P_MZPJ("mzpj","门诊共用平均分摊"),
    P_FTBL("_ftbl","分摊比例"),
    P_FTJE("_ftje","分摊金额"),
    P_BQSR("_bqsr","病区收入(本摊入核算单元"),
    P_BQSR2("_bqsr2","病区收入(本病区)"),
    X_GANGWEI("x_gangwei","岗位系数"),
    X_GANGWEI_ZW("x_gangwei_zw","管理岗位系数（职务）"),
    X_GANGWEI_RY("x_gangwei_ry","专业岗位系数（人员类型）"),
    X_CHUQIN("x_chuqin","出勤系数"),
    X_ZAICE("x_zaice","在册系数"),
    X_CHUQIN_KS("x_chuqin_ks","出勤系数_科室"),
    X_ZAICE_KS("x_zaice_ks","在册系数_科室"),
    X_ZDY("x_zdy","人员考勤自定义"),
    X_ZDY_KS("x_zdy_ks","人员考勤自定义_科室"),
    X_GSKS("x_gsks","人员的归属科室"),
    X_JIANGJIN("x_jiangjin","奖金系数"),
    X_KAOQIN("x_kaoqin","考勤系数"),
    X_COEFFICIENT("x_coefficient","系数"),
    X_SUBSIDY("x_subsidy","补贴"),
    X_DICT_PRO_CATEGORY("x_dict_pro_category","核算项项目分类指标"),
    X_EQUITEMTPRICE("x_equitemtprice", ""),
    X_DEPT_FACTOR("x_dept_factor", "科室系数")
    ;

    private final String type;
    private final String name;

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    FormulaParamEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }
    public static FormulaParamEnum find(String type) {
        for (FormulaParamEnum e : FormulaParamEnum.values()) {
            if (e.getType().equals(type)) {
                return e;
            }
        }
        throw new IllegalArgumentException("未找到类型为" + type + "的枚举");
    }

}
