package com.hscloud.hs.cost.account.utils;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

import java.util.Stack;

public class JepIntUtil extends PostfixMathCommand {
    public JepIntUtil(){
        //初始化参数格式（如参数个数为变长，测为-1）
        super.numberOfParameters = 1;
    }
    @Override
    public void run(Stack stack) throws ParseException {
        //检查栈
        this.checkStack(stack);
        //出栈
        Object obj = stack.pop();
        //计算结果入栈
        stack.push(this.myMethod(obj));
    }
    private Object myMethod(Object obj) throws ParseException{
        if(obj instanceof Number){
            double inputParam=((Number) obj).doubleValue();
            return (int) Math.floor(inputParam);
        }else{
            throw new ParseException("Invalid parameter type");
        }
    }
}
