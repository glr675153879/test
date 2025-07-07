package com.hscloud.hs.cost.account.utils.kpi.aviator;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDecimal;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class ROUND extends AbstractFunction {
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        Number inputParam1 = FunctionUtils.getNumberValue(arg1, env);
        int inputParam2 = FunctionUtils.getNumberValue(arg2, env).intValue();
        //BigDecimal result = new BigDecimal(jep2.getValue()).setScale(allDto.getIndex().getReservedDecimal().intValue(), rule);
        BigDecimal result = new BigDecimal(inputParam1.toString()).setScale(inputParam2, RoundingMode.HALF_UP);
        return new AviatorDecimal(result);
    }

    @Override
    public String getName() {
        return "ROUND";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new IF());
        AviatorEvaluator.addFunction(new INT());
        AviatorEvaluator.addFunction(new DIV());
        AviatorEvaluator.addFunction(new ROUND());
        Expression exp1 = AviatorEvaluator.compile("ROUND(a,b)");
        Map<String, Object> env2 = new HashMap<>();
        env2.put("a",1568.6594);
        env2.put("b",2);
        Object result = exp1.execute(env2);
        System.out.println("result=" + result);
    }
}
