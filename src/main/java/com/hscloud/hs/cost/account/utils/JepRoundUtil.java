package com.hscloud.hs.cost.account.utils;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

import java.math.BigDecimal;
import java.util.Stack;

//自定义函数类（非正数返回0）
public class JepRoundUtil extends PostfixMathCommand {
    public JepRoundUtil(){
        //初始化参数格式（如参数个数为变长，测为-1）
        super.numberOfParameters = 2;
    }
    @Override
    public void run(Stack stack) throws ParseException {
//        //检查栈
//        this.checkStack(stack);
//        //出栈
//        Object obj = stack.pop();
//        Object obj2 = stack.pop();
//        //计算结果入栈
//        stack.push(this.myMethod(obj,obj2));
    }
//    private Object myMethod(Object obj,Object obj2) throws ParseException{
//        if(obj instanceof Number && obj2 instanceof Number){
//            double inputParam=((Number) obj).doubleValue();
//            double inputParam2=((Number) obj2).doubleValue();
//
//            return new BigDecimal(inputParam).setScale(allDto.getIndex().getReservedDecimal().intValue(), rule);
//
//        }else{
//            throw new ParseException("Invalid parameter type");
//        }
//    }
}