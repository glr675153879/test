package com.hscloud.hs.cost.account.utils.kpi.aviator;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBigInt;
import com.googlecode.aviator.runtime.type.AviatorDecimal;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.HashMap;
import java.util.Map;

public class INT extends AbstractFunction {
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
        Number inputParam = FunctionUtils.getNumberValue(arg1, env);
        return new AviatorBigInt((int) Math.floor(inputParam.doubleValue()));
    }

    @Override
    public String getName() {
        return "INT";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new IF());
        AviatorEvaluator.addFunction(new INT());
        Expression exp1 = AviatorEvaluator.compile("INT(-z_bbu_0)");
        Map<String, Object> env2 = new HashMap<>();
        env2.put("z_bbu_0",141.5);
        Object result = exp1.execute(env2);
        System.out.println("result=" + result);
    }
}
