package com.hscloud.hs.cost.account.utils.kpi.aviator;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.*;

import java.util.HashMap;
import java.util.Map;

public class IF extends AbstractFunction {
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2,AviatorObject arg3) {
        boolean condition = FunctionUtils.getBooleanValue(arg1, env);
        Number trueValue =FunctionUtils.getNumberValue(arg2, env);
        Number falseValue = FunctionUtils.getNumberValue(arg3, env);
        // 根据条件返回 trueValue 或 falseValue
        if (condition) {
            return new AviatorDecimal(trueValue); // condition 为真
        } else {
            return new AviatorDecimal(falseValue); // condition 为假
        }
    }

    @Override
    public String getName() {
        return "IF";
    }

    public static void main(String[] args) {
        AviatorEvaluator.addFunction(new IF());
        Expression exp1 = AviatorEvaluator.compile("DIV((((((((h_bcc_0 + h_afm_0) + h_agn_0) + h_agr_0) + h_agu_0) + h_agv_0) + h_alr_0) * z_bao_0), MDAY)");
        Map<String, Object> env2 = new HashMap<>();
        env2.put("h_bcc_0",1);
        env2.put("h_afm_0",2);
        env2.put("z_ayr_0",13);
        env2.put("z_asg_0",9);
        Object result = exp1.execute(env2);
        System.out.println("result=" + result);
    }
}
