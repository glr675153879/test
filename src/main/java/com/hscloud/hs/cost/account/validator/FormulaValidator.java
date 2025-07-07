package com.hscloud.hs.cost.account.validator;

import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;

import java.util.Stack;

/**
 * @author Admin
 * todo 业务逻辑
 */
public abstract class FormulaValidator implements BaseValidator {


    @Override
    public ValidatorResultVo validate(RuleConfig ruleConfig) {
        Long startTime = System.currentTimeMillis();
        ValidatorResultVo validatorResultVo = new ValidatorResultVo();

        // Step 1: Parse and Validate the Formula
        String formula = ruleConfig.getContent();
        if (!isValidFormula(formula)) {
            validatorResultVo.setErrorMsg("公式不合法");
            return validatorResultVo;
        }
        if (!isBizValid(formula)) {
            validatorResultVo.setErrorMsg("业务上不支持同比环比交错");
            return validatorResultVo;
        }
        // Step 2: Evaluate the Formula
        try {
            double result = evaluateFormula(formula);
            validatorResultVo.setResult(result + "");
            Long endTime = System.currentTimeMillis();
            validatorResultVo.setExecuteTime((int) (endTime - startTime));
        } catch (Exception e) {
            validatorResultVo.setErrorMsg("公式执行出错");
        }

        return validatorResultVo;
    }

    /**
     * 业务校验
     *
     * @param formula 公式
     * @return 业务上是否合法
     */
    abstract boolean isBizValid(String formula);

    private boolean isValidFormula(String formula) {
        // Initialize a stack to check for balanced parentheses
        Stack<Character> parenthesesStack = new Stack<>();

        for (char c : formula.toCharArray()) {
            // Check for valid characters (digits, operators, parentheses, and whitespace)
            if (!(Character.isDigit(c) || isOperator(c) || c == '(' || c == ')' || Character.isWhitespace(c))) {
                return false;
            }

            // Check for balanced parentheses
            if (c == '(') {
                parenthesesStack.push(c);
            } else if (c == ')') {
                if (parenthesesStack.isEmpty() || parenthesesStack.pop() != '(') {
                    return false;
                }
            }
        }

        // Ensure all parentheses are balanced
        if (!parenthesesStack.isEmpty()) {
            return false;
        }

        // Check for valid operator positions
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (isOperator(c)) {
                // Operators cannot be the first or last character
                if (i == 0 || i == formula.length() - 1) {
                    return false;
                }

                // Operators cannot be adjacent to each other
                char prevChar = formula.charAt(i - 1);
                char nextChar = formula.charAt(i + 1);
                if (isOperator(prevChar) || isOperator(nextChar)) {
                    return false;
                }
            }
        }

        return true;
    }


    private double evaluateFormula(String formula) {
        // Implement formula evaluation logic here
        // You can use a third-party library like Apache Commons Math or write a custom parser.
        // For simplicity, we'll evaluate a basic formula without variables and functions.

        Stack<Double> operands = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (char c : formula.toCharArray()) {
            if (Character.isDigit(c)) {
                operands.push((double) Character.getNumericValue(c));
            } else if (isOperator(c)) {
                while (!operators.isEmpty() && hasHigherPrecedence(c, operators.peek())) {
                    double operand2 = operands.pop();
                    double operand1 = operands.pop();
                    char operator = operators.pop();
                    double result = applyOperator(operand1, operand2, operator);
                    operands.push(result);
                }
                operators.push(c);
            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    double operand2 = operands.pop();
                    double operand1 = operands.pop();
                    char operator = operators.pop();
                    double result = applyOperator(operand1, operand2, operator);
                    operands.push(result);
                }
                operators.pop(); // Pop the '('
            }
        }

        while (!operators.isEmpty()) {
            double operand2 = operands.pop();
            double operand1 = operands.pop();
            char operator = operators.pop();
            double result = applyOperator(operand1, operand2, operator);
            operands.push(result);
        }

        return operands.pop();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private boolean hasHigherPrecedence(char op1, char op2) {
        return (op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-');
    }

    private double applyOperator(double a, double b, char operator) {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a / b;
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }

}
