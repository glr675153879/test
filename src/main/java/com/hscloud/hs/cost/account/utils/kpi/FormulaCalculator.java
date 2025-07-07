package com.hscloud.hs.cost.account.utils.kpi;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class FormulaCalculator {
//求和
public static class SumFunction extends PostfixMathCommand {


    public SumFunction(){
        numberOfParameters = -1; //可变参数
    }

    @Override
    public void run(Stack inStack) throws ParseException {
        //检查栈
        this.checkStack(inStack);
        List<Object> params = new ArrayList<>();
        for(int i=0;i<this.curNumberOfParameters;i++){
            params.add(inStack.pop());
        }
        inStack.push(this.sum(params));
    }

    public Object sum(List<Object> params) throws ParseException {
        double sum=0.0;
        for (Object param : params) {
            if (param instanceof Number) {
                sum += ((Number)param).doubleValue();
            } else {
                throw new ParseException("Invalid parameter type");
            }
        }
        return sum;
    }

}

//求总数
class CountFunction extends PostfixMathCommand{

    public CountFunction(){
        numberOfParameters = -1; //可变参数
    }
    @Override
    public void run(Stack inStack) throws ParseException {
        //检查栈
        this.checkStack(inStack);
        List<Object> params = new ArrayList<>();
        for(int i=0;i<this.curNumberOfParameters;i++){
            params.add(inStack.pop());
        }
//        while(!inStack.isEmpty()){
//            params.add(inStack.pop());
//        }
        inStack.push(this.count(params));
    }
    public Object count(List<Object> params) throws ParseException {
        double count=0.0;
        for (Object param : params) {
            if (param instanceof Number) {
                count += 1;
            } else {
                throw new ParseException("Invalid parameter type");
            }
        }
        return count;
    }

}

//求最小值
public static class MinFunction extends PostfixMathCommand {

    public MinFunction() {
        numberOfParameters = -1; //可变参数
    }

    @Override
    public void run(Stack inStack) throws ParseException {
        //检查栈
        this.checkStack(inStack);
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < this.curNumberOfParameters; i++) {
            params.add(inStack.pop());
        }
        inStack.push(this.min(params));

    }
    public Object min(List<Object> params) throws ParseException{
        //快速排序，返回最小值，也就是数组的第一个元素
        //将object转换为double
        List<Double> list = new ArrayList<>();
        for (Object param : params) {
            if (param instanceof Number) {
                list.add(((Number) param).doubleValue());
            } else {
                throw new ParseException("Invalid parameter type");
            }
        }
        List<Double> newList= com.neu.statistics.utils.QuickSortUtil.quickSort(list,0,list.size()-1);
        Object result=newList.get(0);
        return result;
    }

}

//求最大值
public static class MaxFunction extends PostfixMathCommand {

    public MaxFunction() {
        numberOfParameters = -1; //可变参数
    }

    @Override
    public void run(Stack inStack) throws ParseException {
        //检查栈
        this.checkStack(inStack);
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < this.curNumberOfParameters; i++) {
            params.add(inStack.pop());
        }

        inStack.push(this.max(params));
    }
    public Object max(List<Object> params) throws ParseException{
        //快速排序，返回最小值，也就是数组的第一个元素
        //将object转换为double
        List<Double> list = new ArrayList<>();
        for (Object param : params) {
            if (param instanceof Number) {
                list.add(((Number) param).doubleValue());
            } else {
                throw new ParseException("Invalid parameter type");
            }
        }
        List<Double> newList= com.neu.statistics.utils.QuickSortUtil.quickSort(list,0,list.size()-1);
        Object result=newList.get(list.size()-1);
        return result;
    }

}

//中位数
class MedianFunction extends PostfixMathCommand {

    public MedianFunction() {
        numberOfParameters = -1; //可变参数
    }

    @Override
    public void run(Stack inStack) throws ParseException {
        //检查栈
        this.checkStack(inStack);
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < this.curNumberOfParameters; i++) {
            params.add(inStack.pop());
        }
        inStack.push(this.median(params));
    }
    public Object median(List<Object> params) throws ParseException{
        //快速排序，返回最小值，也就是数组的第一个元素
        //将object转换为double
        List<Double> list = new ArrayList<>();
        for (Object param : params) {
            if (param instanceof Number) {
                list.add(((Number) param).doubleValue());
            } else {
                throw new ParseException("Invalid parameter type");
            }
        }
        List<Double> newList= com.neu.statistics.utils.QuickSortUtil.quickSort(list,0,list.size()-1);
        int length=newList.size();
        if(newList.size()%2==0){
            Object result=(newList.get(length/2)+newList.get(length/2-1))/2;
            return result;
        }
        else{
            Object result=newList.get(length/2);
            return result;
        }
    }

}

//方差
class VarianceFunction extends PostfixMathCommand {

    public VarianceFunction() {
        numberOfParameters = -1; //可变参数
    }

    @Override
    public void run(Stack inStack) throws ParseException {
        //检查栈
        this.checkStack(inStack);
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < this.curNumberOfParameters; i++) {
            params.add(inStack.pop());
        }
        inStack.push(this.variance(params));
    }
    public Object variance(List<Object> params) throws ParseException{
        SumFunction sf=new SumFunction();
        double sum=(double)sf.sum(params);
        CountFunction cf=new CountFunction();
        double count=(double)cf.count(params);
        double average=sum/count;
        //快速排序，返回最小值，也就是数组的第一个元素
        //将object转换为double
        List<Double> list = new ArrayList<>();
        for (Object param : params) {
            if (param instanceof Number) {
                list.add(((Number) param).doubleValue());
            } else {
                throw new ParseException("Invalid parameter type");
            }
        }
        List<Double> newList= com.neu.statistics.utils.QuickSortUtil.quickSort(list,0,list.size()-1);
        double sumOfSquaredDifferences = 0.0;
        for(double value:newList){
            double difference=value-average;
            sumOfSquaredDifferences+=difference*difference;
        }
        Object result=sumOfSquaredDifferences/count;
        return result;
    }

}

    //自定义函数div
    public static class DivFunction extends PostfixMathCommand {
        public DivFunction(){
            //初始化参数格式（如参数个数为变长，测为-1）
            super.numberOfParameters = 2;
        }
        @Override
        public void run(Stack stack) throws ParseException {
            //检查栈
            this.checkStack(stack);
            Object obj = stack.pop();
            Object obj2 = stack.pop();
            if(obj instanceof Number && obj2 instanceof Number){
                double inputParam=((Number) obj).doubleValue();
                double inputParam2=((Number) obj2).doubleValue();
                if(inputParam==0||inputParam2==0){
                    stack.push(new Double("0.00"));
                }else{
                    stack.push(new Double(inputParam2/inputParam));
                }
            }else{
                throw new ParseException("Invalid parameter type");
            }
        }
    }

    //IF
    public static class IfFunction extends PostfixMathCommand {

        public IfFunction() {
            // IF function takes 3 arguments: condition, trueValue, falseValue
            this.numberOfParameters = 3;
        }

        @Override
        public void run(Stack stack) throws ParseException {
            // 确保栈上有足够的参数
            if (stack.size() < numberOfParameters) {
                throw new ParseException("Not enough parameters for IF function");
            }

            // 弹出参数
            double falseValue = ((Number) stack.pop()).doubleValue(); // falseValue
            double trueValue = ((Number) stack.pop()).doubleValue();   // trueValue
            double condition = ((Number) stack.pop()).doubleValue();    // condition

            // 根据条件返回 trueValue 或 falseValue
            if (condition != 0) {
                stack.push(trueValue); // condition 为真
            } else {
                stack.push(falseValue); // condition 为假
            }
        }
    }
//=判断
    public static class EqualsFunction extends PostfixMathCommand {

        public EqualsFunction() {
            // 等于判断符号需要两个参数
            this.numberOfParameters = 2;
        }

        @Override
        public void run(Stack stack) throws ParseException {
            // 确保栈上有足够的参数
            if (stack.size() < numberOfParameters) {
                throw new ParseException("Not enough parameters for equality check");
            }

            // 弹出两个参数
            double value2 = ((Number) stack.pop()).doubleValue(); // 第二个值
            double value1 = ((Number) stack.pop()).doubleValue(); // 第一个值

            // 判断两个值是否相等
            boolean isEqual = (value1 == value2);

            // 将结果推回栈中，使用 1 和 0 表示 true 和 false
            stack.push(isEqual ? 1 : 0);
        }
    }
//>=
    public static class GreaterThanOrEqualFunction extends PostfixMathCommand {

        public GreaterThanOrEqualFunction() {
            // 大于等于判断符号需要两个参数
            this.numberOfParameters = 2;
        }

        @Override
        public void run(Stack stack) throws ParseException {
            // 确保栈上有足够的参数
            if (stack.size() < numberOfParameters) {
                throw new ParseException("Not enough parameters for >= check");
            }

            // 弹出两个参数
            double value2 = ((Number) stack.pop()).doubleValue(); // 第二个值
            double value1 = ((Number) stack.pop()).doubleValue(); // 第一个值

            // 判断第一个值是否大于等于第二个值
            boolean isGreaterThanOrEqual = (value1 >= value2);

            // 将结果推回栈中，使用 1 和 0 表示 true 和 false
            stack.push(isGreaterThanOrEqual ? 1.0 : 0.0);
        }
    }

    //<=
    public static class LessThanOrEqualFunction extends PostfixMathCommand {

        public LessThanOrEqualFunction() {
            // 小于等于判断符号需要两个参数
            this.numberOfParameters = 2;
        }

        @Override
        public void run(Stack stack) throws ParseException {
            // 确保栈上有足够的参数
            if (stack.size() < numberOfParameters) {
                throw new ParseException("Not enough parameters for <= check");
            }

            // 弹出两个参数
            double value2 = ((Number) stack.pop()).doubleValue(); // 第二个值
            double value1 = ((Number) stack.pop()).doubleValue(); // 第一个值

            // 判断第一个值是否小于或等于第二个值
            boolean isLessThanOrEqual = (value1 <= value2);

            // 将结果推回栈中，使用 1 和 0 表示 true 和 false
            stack.push(isLessThanOrEqual ? 1 : 0);
        }
    }

}