package com.hscloud.hs.cost.account.utils.kpi.aviator;

import com.alibaba.fastjson.JSONObject;
import com.bestvike.linq.Linq;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBigInt;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class DIV extends AbstractFunction {
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        double inputParam1=0;
        double inputParam2=0;
        if (FunctionUtils.getNumberValue(arg1,env) !=null){
            inputParam1 = FunctionUtils.getNumberValue(arg1, env).doubleValue();
        }else {
            return new AviatorDouble(0);
        }
        if (FunctionUtils.getNumberValue(arg2,env) !=null){
            inputParam2 = FunctionUtils.getNumberValue(arg2, env).doubleValue();
        }else {
            return new AviatorDouble(0);
        }
        if(inputParam1==0||inputParam2==0){
            return new AviatorDouble(0);
        }else{
            return new AviatorDouble(inputParam1/inputParam2);
        }
    }

    @Override
    public String getName() {
        return "DIV";
    }

    public static void main(String[] args) {
        /*AviatorEvaluator.addFunction(new IF());
        AviatorEvaluator.addFunction(new INT());
        AviatorEvaluator.addFunction(new DIV());
// -- 1. 解析浮点数为 Decimal 类型
        AviatorEvaluator.getInstance().setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
// -- 2. 解析整数为 Decimal 类型
        AviatorEvaluator.getInstance().setOption(Options.ALWAYS_PARSE_INTEGRAL_NUMBER_INTO_DECIMAL, true);
        Map<String, Object> env2 = new HashMap<>();
        env2.put("a",null);
        env2.put("b",0);
        Object result = AviatorEvaluator.execute("70*0.01",env2);
        //BigDecimal result2 = new BigDecimal(result.toString()).setScale(10, RoundingMode.HALF_UP);
        System.out.println("result=" + result);*/
        Long period = 202401L;
        if(((period-1)+"").endsWith("00")){

            System.out.println(period.toString().substring(0, 4)+"-"+period.toString().substring(4, 6));
        }
        List<Long> depts = Linq.of(Arrays.asList("1,2,3,5".split(","))).select(m -> Long.parseLong(m)).toList();
        List<Long> removeDepts = Linq.of(Arrays.asList("2,3,5,6".split(","))).select(m -> Long.parseLong(m)).toList();
        depts.removeAll(removeDepts);
        System.out.println(JSONObject.toJSONString(depts));
    }
}
