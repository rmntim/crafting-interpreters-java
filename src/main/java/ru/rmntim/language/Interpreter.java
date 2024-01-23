package ru.rmntim.language;

import ru.rmntim.language.token.Token;
import ru.rmntim.language.token.TokenType;

import java.util.List;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {
    private Environment environment = new Environment();

    public void interpret(final List<Statement> statements) {
        try {
            for (var statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            ErrorReporter.runtimeError(error);
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

    @Override
    public Object visit(Expression.Variable expression) {
        return environment.get(expression.getName());
    }

    @Override
    public Object visit(Expression.Assignment expression) {
        var value = evaluate(expression.getValue());
        environment.assign(expression.getName(), value);
        return value;
    }

    @Override
    public Object visit(Expression.Ternary expression) {
        if (isTruthy(evaluate(expression.getCondition()))) {
            return evaluate(expression.getThenBranch());
        } else {
            return evaluate(expression.getElseBranch());
        }
    }

    @Override
    public Object visit(Expression.Logical expression) {
        var left = evaluate(expression.getLeft());

        if (expression.getOperator().type() == TokenType.OR) {
            if (isTruthy(left)) {
                return left;
            }
        } else {
            if (!isTruthy(left)) {
                return left;
            }
        }

        return evaluate(expression.getRight());
    }

    @Override
    public Void visit(Statement.Expr statement) {
        evaluate(statement.getExpression());
        return null;
    }

    @Override
    public Void visit(Statement.Print statement) {
        var value = evaluate(statement.getExpression());
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visit(Statement.Let statement) {
        var variable = new Variable();

        if (statement.getInitializer() != null) {
            variable.setValue(evaluate(statement.getInitializer()));
            variable.setInitialized();
        }

        environment.define(statement.getName().literal(), variable);
        return null;
    }

    @Override
    public Void visit(Statement.Block statement) {
        executeBlock(statement.getStatements(), new Environment(environment));
        return null;
    }

    @Override
    public Void visit(Statement.If statement) {
        if (isTruthy(evaluate(statement.getCondition()))) {
            execute(statement.getThenBranch());
        } else if (statement.getElseBranch().isPresent()) {
            execute(statement.getElseBranch().get());
        }
        return null;
    }

    private void executeBlock(List<Statement> statements, Environment environment) {
        var previousEnv = this.environment;
        try {
            this.environment = environment;

            for (var statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previousEnv;
        }
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

    private void execute(Statement statement) {
        statement.accept(this);
    }
}
