package com.example.byaj_calculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.DecimalFormat;

public class CalculatorFragment extends Fragment {

    private TextView tvExpression, tvResult;
    private StringBuilder expression = new StringBuilder();
    private boolean justCalculated = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calculator, container, false);

        tvExpression = view.findViewById(R.id.tvExpression);
        tvResult = view.findViewById(R.id.tvResult);

        int[] numberBtnIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };
        String[] numbers = {"0","1","2","3","4","5","6","7","8","9"};

        for (int i = 0; i < numberBtnIds.length; i++) {
            final String num = numbers[i];
            view.findViewById(numberBtnIds[i]).setOnClickListener(v -> appendToExpression(num));
        }

        view.findViewById(R.id.btnDot).setOnClickListener(v -> appendDot());
        view.findViewById(R.id.btnPlus).setOnClickListener(v -> appendOperator("+"));
        view.findViewById(R.id.btnMinus).setOnClickListener(v -> appendOperator("-"));
        view.findViewById(R.id.btnMultiply).setOnClickListener(v -> appendOperator("×"));
        view.findViewById(R.id.btnDivide).setOnClickListener(v -> appendOperator("÷"));
        view.findViewById(R.id.btnPercent).setOnClickListener(v -> appendOperator("%"));
        view.findViewById(R.id.btnEquals).setOnClickListener(v -> calculate());
        view.findViewById(R.id.btnClear).setOnClickListener(v -> clear());
        view.findViewById(R.id.btnBackspace).setOnClickListener(v -> backspace());
        view.findViewById(R.id.btnPlusMinus).setOnClickListener(v -> toggleSign());

        return view;
    }

    private void appendToExpression(String value) {
        if (justCalculated) {
            // if last was a number, start fresh; if operator, continue
            justCalculated = false;
            if (!isOperator(value)) {
                expression.setLength(0);
                tvExpression.setText("");
            }
        }
        expression.append(value);
        tvExpression.setText(expression.toString());
        showLiveResult();
    }

    private void appendOperator(String op) {
        justCalculated = false;
        if (expression.length() == 0) {
            if (op.equals("-")) {
                expression.append(op);
                tvExpression.setText(expression.toString());
            }
            return;
        }
        char last = expression.charAt(expression.length() - 1);
        if (isOperatorChar(last) && last != '-') {
            expression.setCharAt(expression.length() - 1, op.charAt(0));
        } else {
            expression.append(op);
        }
        tvExpression.setText(expression.toString());
    }

    private void appendDot() {
        justCalculated = false;
        // Find the last number segment and check if it already has a dot
        String expr = expression.toString();
        int lastOpIndex = -1;
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == '+' || c == '-' || c == '×' || c == '÷' || c == '%') {
                lastOpIndex = i;
                break;
            }
        }
        String lastNumber = expr.substring(lastOpIndex + 1);
        if (!lastNumber.contains(".")) {
            if (lastNumber.isEmpty()) expression.append("0");
            expression.append(".");
            tvExpression.setText(expression.toString());
        }
    }

    private void backspace() {
        if (expression.length() > 0) {
            expression.deleteCharAt(expression.length() - 1);
            tvExpression.setText(expression.toString());
            showLiveResult();
        }
    }

    private void clear() {
        expression.setLength(0);
        tvExpression.setText("");
        tvResult.setText("0");
        justCalculated = false;
    }

    private void toggleSign() {
        String expr = expression.toString();
        if (expr.isEmpty()) return;

        // Find the last number and negate it
        int lastOpIndex = -1;
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == '+' || c == '×' || c == '÷' || c == '%') {
                lastOpIndex = i;
                break;
            }
            if (c == '-' && i > 0) {
                lastOpIndex = i;
                break;
            }
        }

        if (lastOpIndex == -1) {
            // Only one number
            if (expr.startsWith("-")) {
                expression.deleteCharAt(0);
            } else {
                expression.insert(0, "-");
            }
        } else {
            String after = expr.substring(lastOpIndex + 1);
            String before = expr.substring(0, lastOpIndex + 1);
            if (after.startsWith("-")) {
                expression.setLength(0);
                expression.append(before).append(after.substring(1));
            } else if (!after.isEmpty()) {
                expression.setLength(0);
                expression.append(before).append("-").append(after);
            }
        }
        tvExpression.setText(expression.toString());
        showLiveResult();
    }

    private void showLiveResult() {
        try {
            String expr = expression.toString();
            if (expr.isEmpty()) { tvResult.setText("0"); return; }
            double result = evaluate(expr);
            if (!Double.isNaN(result) && !Double.isInfinite(result)) {
                tvResult.setText(formatResult(result));
            }
        } catch (Exception ignored) {}
    }

    private void calculate() {
        try {
            String expr = expression.toString();
            if (expr.isEmpty()) return;
            double result = evaluate(expr);
            if (Double.isInfinite(result)) {
                tvResult.setText("Error");
                return;
            }
            String formatted = formatResult(result);
            tvResult.setText(formatted);
            expression.setLength(0);
            expression.append(formatted.replace(",", ""));
            tvExpression.setText(expr + " =");
            justCalculated = true;
        } catch (Exception e) {
            tvResult.setText("Error");
        }
    }

    private double evaluate(String expr) {
        // Replace display operators with actual ones
        expr = expr.replace("×", "*").replace("÷", "/");

        // Handle percentage: number% = number/100
        expr = expr.replaceAll("(\\d+\\.?\\d*)%", "($1/100)");

        return evalExpression(expr);
    }

    // Simple recursive expression evaluator
    private double evalExpression(String expr) {
        expr = expr.trim();

        // Find last + or - (not inside parens) for addition/subtraction
        int parenDepth = 0;
        int lastPlusMinusIndex = -1;
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == ')') parenDepth++;
            else if (c == '(') parenDepth--;
            else if (parenDepth == 0 && (c == '+' || c == '-') && i > 0) {
                lastPlusMinusIndex = i;
                break;
            }
        }

        if (lastPlusMinusIndex > 0) {
            double left = evalExpression(expr.substring(0, lastPlusMinusIndex));
            double right = evalExpression(expr.substring(lastPlusMinusIndex + 1));
            return expr.charAt(lastPlusMinusIndex) == '+' ? left + right : left - right;
        }

        // Find last * or /
        parenDepth = 0;
        int lastMulDivIndex = -1;
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == ')') parenDepth++;
            else if (c == '(') parenDepth--;
            else if (parenDepth == 0 && (c == '*' || c == '/')) {
                lastMulDivIndex = i;
                break;
            }
        }

        if (lastMulDivIndex >= 0) {
            double left = evalExpression(expr.substring(0, lastMulDivIndex));
            double right = evalExpression(expr.substring(lastMulDivIndex + 1));
            return expr.charAt(lastMulDivIndex) == '*' ? left * right : left / right;
        }

        // Parentheses
        if (expr.startsWith("(") && expr.endsWith(")")) {
            return evalExpression(expr.substring(1, expr.length() - 1));
        }

        // Negative number
        if (expr.startsWith("-")) {
            return -evalExpression(expr.substring(1));
        }

        return Double.parseDouble(expr);
    }

    private String formatResult(double result) {
        if (result == Math.floor(result) && !Double.isInfinite(result) && Math.abs(result) < 1e15) {
            return String.valueOf((long) result);
        }
        DecimalFormat df = new DecimalFormat("#.##########");
        return df.format(result);
    }

    private boolean isOperator(String s) {
        return s.equals("+") || s.equals("-") || s.equals("×") || s.equals("÷") || s.equals("%");
    }

    private boolean isOperatorChar(char c) {
        return c == '+' || c == '-' || c == '×' || c == '÷' || c == '%';
    }
}