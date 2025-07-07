package com.hscloud.hs.cost.account.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.graphbuilder.math.func.MaxFunction;
import com.hscloud.hs.cost.account.utils.kpi.FormulaCalculator;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.connect.data.Decimal;
import org.jetbrains.annotations.NotNull;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.Add;
import org.nfunk.jep.function.Divide;
import org.nfunk.jep.function.PostfixMathCommand;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionCheckHelper {




    private static final Pattern VARIABLE_PATTERN = Pattern.compile("[a-zA-Z]+[a-zA-Z0-9]*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\b\\d+\\b");

    public static BigDecimal aviatorCacu(String formula,Map<String, Double> map)
    {
        Map<String, Object> newmap=new HashMap<>();
        map.forEach((q,w)->{
            newmap.put(q,w);
        });
        return aviatorCacu(formula,newmap,0L,BigDecimal.ZERO);
    }


    public static BigDecimal aviatorCacu(String formula,Map<String, Object> map,Long period,BigDecimal equitemtprice) {
        int mday=0;
        String s = period.toString();
        if (s.length() == 6) {
        int month = Integer.parseInt(s.substring(4, 6));
        int year = Integer.parseInt(s.substring(0, 4));
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1); // 设置为指定年份和月份的第一天 因为Calendar的月份从0开始计数
        //Date time = calendar.getTime();
        //System.out.println(time);
            mday= calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        map.put("MDAY",mday);
        // 当量单价 系统指标 X_EQUITEMTPRICE
        //map.put("EQUITEMTPRICE",equitemtprice);
        Object result = AviatorEvaluator.execute(formula,map);
        return new BigDecimal(result.toString());
    }

    public static String checkAndCalculateKpi(Map<String,Double> map, String expression, String unit, Integer figure,String ruleName) {
        RoundingMode rule = getRoundingMode(ruleName);
        boolean ratio=false;
        if ("%".equals(unit)){
            ratio=true;
        }
        if (StrUtil.isNotBlank(expression)){
//            int index = expression.indexOf("=");
//            if (index>=0&&expression.length()>1){
//                String lv = expression.substring(0, index + 1);
//                if (lv.contains("增长率")||lv.contains("同比")||lv.contains("环比")){
//                    ratio=true;
//                }
//                expression= expression.substring(index+1);
//            }
        }
        figure= null==figure?2:figure;
        boolean tenThousand=false;
        if (StrUtil.isNotBlank(unit)){
            if (unit.contains("万")){
                tenThousand= true;
            }
        }else{
            unit="";
        }

        BigDecimal  rt= aviatorCacu( expression,map);

        try {

            if (ratio) {
                String result = rt.multiply(new BigDecimal("100")).setScale(figure, rule).toString();
                if ("∞".equals(result)) {
                    return "分母不允许为0";
                }
                return result + "%";
            }else if(tenThousand){
                String result = rt.divide(new BigDecimal("10000")).setScale(figure, rule).toString();
                if ("∞".equals(result)) {
                    return "分母不允许为0";
                }
                return result + unit;
            }else {
                String result = rt.setScale(figure, rule).toString();
                if ("∞".equals(result)) {
                    return "分母不允许为0";
                }

                return result;
            }
        }catch (ArithmeticException e){
            throw new ArithmeticException("分母不允许为0");
        }
    }


    /**
     * * 赵家东
     *  表达式校验并计算
     *  map 变量键值对
     *  expression 表达式
     *  unit 单位，例如：万元
     *  figure 小数位为null时默认两位小数
     *  ruleName 进位规则为null时默认四舍五入
     */
    public static String checkAndCalculate(Map<String,Double> map, String expression, String unit, Integer figure,String ruleName) {
        RoundingMode rule = getRoundingMode(ruleName);
        boolean ratio=false;
        if ("%".equals(unit)){
            ratio=true;
        }
        if (StrUtil.isNotBlank(expression)){
            int index = expression.indexOf("=");
            if (index>=0&&expression.length()>1){
                String lv = expression.substring(0, index + 1);
                if (lv.contains("增长率")||lv.contains("同比")||lv.contains("环比")){
                    ratio=true;
                }
                expression= expression.substring(index+1);
            }
        }
        figure= null==figure?2:figure;
        boolean tenThousand=false;
        if (StrUtil.isNotBlank(unit)){
            if (unit.contains("万")){
                tenThousand= true;
            }
        }else{
            unit="";
        }

        var  engine= my(map, expression);

        try {

            if (ratio) {
                String result = new BigDecimal(engine.getValueAsObject().toString()).multiply(new BigDecimal("100")).setScale(figure, rule).toString();
                if ("∞".equals(result)) {
                    return "分母不允许为0";
                }
                return result + "%";
            }else if(tenThousand){
                String result = new BigDecimal(engine.getValueAsObject().toString()).divide(new BigDecimal("10000")).setScale(figure, rule).toString();
                if ("∞".equals(result)) {
                    return "分母不允许为0";
                }
                return result + unit;
            }else {
                String result = new BigDecimal(engine.getValueAsObject().toString()).setScale(figure, rule).toString();
                if ("∞".equals(result)) {
                    return "分母不允许为0";
                }

                return result;
            }
        }catch (ArithmeticException e){
            throw new ArithmeticException("分母不允许为0");
        }
    }

    public static String expressionTransformationKpi(String expression){
        String result=expression.replace("（","(").replace("）",")").replace("【","[").replace("】","]").replace("[","_").replace("]","").replaceAll("\\s*", "");
//        result = result.replaceAll("MDAY", String.valueOf(DateUtil.lengthOfMonth(DateUtil.month(new Date())+1,DateUtil.isLeapYear(DateUtil.year(new Date())))));
        return result;
    }

    /**
     * * 赵家东
     * 表达式校验转换
     */
    public static String expressionTransformation(String expression){
        if (StrUtil.isNotBlank(expression)){
            int index = expression.indexOf("=");
            if (index>=0&&expression.length()>1){
                expression= expression.substring(index+1);
            }
        }
        String result=expression.replace("（","(").replace("）",")").replace("【","[").replace("】","]").replace("[","_").replace("]","").replaceAll("\\s*", "");
        return result;
    }

    /**
     * * 赵家东
     * 表达式校验元素去重，并拣每个元素出来
     */
    public static Set<String> expressionQuantity(String expression) {
        Set<String> set = new LinkedHashSet<>();
        if (expression != null) {
            int index = expression.indexOf("=");
            if (index > -1 && expression.length() > 0) {
                expression = expression.substring(index + 1);
            }

            Matcher matcher = VARIABLE_PATTERN.matcher(expression);
            while (matcher.find()) {
                String item = matcher.group();
                set.add(item);
            }
        }
        return set;
    }

    /**
     * * 赵家东
     *  单纯计算
     *  map 变量键值对
     *  expression 表达式
     *  unit 单位，例如：万元
     *  figure 小数位为null时默认两位小数
     *  ruleName 进位规则为null时默认四舍五入
     */
    public static String calculate(Map<String,Double> map, String expression) {


        JEP engine = my(map, expression);
        String result = engine.getValueAsObject().toString();

        return result;
    }

    public static void main(String[] args) {
        Map<String, Double> map = new HashMap<>();

    }

     /*@NotNull
   public static JEP myKpi(Map<String, Double> map, String expression) {
        JEP jep = new JEP();
        jep.setImplicitMul(true);
        jep.addFunction("DIV", new JepDivUtil());
        jep.addFunction("INT", new JepIntUtil());
        jep.addFunction("MAX",new FormulaCalculator.MaxFunction());
        jep.addFunction("MIN",new FormulaCalculator.MinFunction());
        jep.addFunction("IF",new FormulaCalculator.IfFunction());
        //        =号有问题,自带==
        //        jep.addFunction("=",new FormulaCalculator.EqualsFunction());
        jep.addFunction(">=",new FormulaCalculator.GreaterThanOrEqualFunction());
        jep.addFunction("<=",new FormulaCalculator.LessThanOrEqualFunction());

        Set<Map.Entry<String, Double>> entries = map.entrySet();
        for(Map.Entry<String,Double> en : entries){
            jep.addVariable(en.getKey(),en.getValue());
        }
        String s = expressionTransformationKpi(expression);
        jep.parseExpression(s);
        if (StringUtils.isNotBlank(jep.getErrorInfo())){
            throw new BizException(jep.getErrorInfo());
        }
        return jep;
    }*/

    /*@NotNull
    public static JEP myKpi(JEP jep ,Map<String, Double> map, String expression) {
        jep.addFunction("DIV", new JepDivUtil());
        jep.addFunction("INT", new JepIntUtil());
        jep.addFunction("MAX",new FormulaCalculator.MaxFunction());
        jep.addFunction("MIN",new FormulaCalculator.MinFunction());
        jep.addFunction("IF",new FormulaCalculator.IfFunction());
        //        =号有问题,自带==
        //        jep.addFunction("=",new FormulaCalculator.EqualsFunction());
        jep.addFunction(">=",new FormulaCalculator.GreaterThanOrEqualFunction());
        jep.addFunction("<=",new FormulaCalculator.LessThanOrEqualFunction());

        Set<Map.Entry<String, Double>> entries = map.entrySet();
        for(Map.Entry<String,Double> en : entries){
            jep.addVariable(en.getKey(),en.getValue());
        }
        String s = expressionTransformationKpi(expression);
        jep.parseExpression(s);
        if (StringUtils.isNotBlank(jep.getErrorInfo())){
            throw new BizException(jep.getErrorInfo());
        }
        return jep;
    }*/

    @NotNull
    private static JEP my(Map<String, Double> map, String expression) {
        JEP jep = new JEP();
        Set<Map.Entry<String, Double>> entries = map.entrySet();
        for(Map.Entry<String,Double> en : entries){
            jep.addVariable(en.getKey(),en.getValue());
        }
        jep.parseExpression(expressionTransformation(expression));
        return jep;
    }

    @NotNull
    private static RoundingMode getRoundingMode(String ruleName) {
        //默认值
        RoundingMode rule= RoundingMode.HALF_UP;
        if (StrUtil.isBlank(ruleName)||"四舍五入".equals(ruleName.trim())){
            rule=RoundingMode.HALF_UP;
        }else if ("向上取整".equals(ruleName.trim())){
            rule=RoundingMode.CEILING;
        }else if("向下取整".equals(ruleName.trim())){
            rule=RoundingMode.FLOOR;
        }
        return rule;
    }

    /**
     * * 此方法用于解析对象id并返回id集合
     * @param str
     * @return
     * @throws Exception
     */
    public static List<String> getIds(String str) {
        Matcher matcher = DIGIT_PATTERN.matcher(str);
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            String result = matcher.group();
            list.add(result);
        }
        return list;
    }


}
