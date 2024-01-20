package ru.rmntim.language;

import ru.rmntim.language.token.Token;
import ru.rmntim.language.util.Logger;

public class Interpreter implements Expression.Visitor<Object> {
    public void interpret(Expression expression) {
        try {
            var value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Logger.runtimeError(error);
        }
    }

    private String stringify(Object value) {
        if (value == null) {
            return "nil";
        }

        if (value instanceof Double) {
            var text = value.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return value.toString();
    }

    @Override
    public Object visit(Expression.Literal expression) {
        return expression.getValue();
    }

    @Override
    public Object visit(Expression.Grouping expression) {
        return evaluate(expression.getSubExpression());
    }

    @Override
    public Object visit(Expression.Unary expression) {
        var right = evaluate(expression.getRight());

        return switch (expression.getOperator().type()) {
            case MINUS -> {
                checkNumberOperands(expression.getOperator(), right);
                yield -(double) right;
            }
            case BANG -> !isTruthy(right);
            default -> null;
        };
    }

    @Override
    public Object visit(Expression.Binary expression) {
        var left = evaluate(expression.getLeft());
        var right = evaluate(expression.getRight());

        return switch (expression.getOperator().type()) {
            case MINUS -> {
                checkNumberOperands(expression.getOperator(), left, right);
                yield (double) left - (double) right;
            }
            case SLASH -> {
                checkNumberOperands(expression.getOperator(), left, right);
                yield (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperands(expression.getOperator(), left, right);
                yield (double) left * (double) right;
            }
            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    yield (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    yield left + (String) right;
                }
                if (left instanceof String) {
                    yield left + stringify(right);
                }
                if (right instanceof String) {
                    yield stringify(left) + right;
                }

                throw new RuntimeError(expression.getOperator(), "Operands must be either strings or numbers");
            }
            case GREATER -> {
                checkNumberOperands(expression.getOperator(), left, right);
                yield (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expression.getOperator(), left, right);
                yield (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expression.getOperator(), left, right);
                yield (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expression.getOperator(), left, right);
                yield (double) left <= (double) right;
            }
            case BANG_EQUAL -> !isEqual(left, right);
            case EQUAL_EQUAL -> isEqual(left, right);
            default -> null;
        };
    }

    private void checkNumberOperands(Token operator, Object... operands) {
        for (var operand : operands) {
            if (!(operand instanceof Double)) {
                throw new RuntimeError(operator, "Operand must be a number");
            }
        }
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null) {
            return false;
        }
        return left.equals(right);
    }

    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        return true;
    }

    private Object evaluate(Expression expression) {
        return expression.accept(this);
    }
}
