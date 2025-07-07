package com.hscloud.hs.cost.account.generator;

public class Generator {

    public static void main(String[] args) throws Exception {
        /**
         * 构造方法
         * @param comPathDot com.hscloud.hs.cost.account
         * @param moduleName /hs-cost-account  或  /hs-cost-account/hs-cost-biz
         * @param childSuf second 可空
         */
        GeneratorUtil generatorUtil = new GeneratorUtil("com.hscloud.hs.cost.account", "/hs-cost-account", "deptCost", "Dc");
        generatorUtil.scanEntity();
    }
}
