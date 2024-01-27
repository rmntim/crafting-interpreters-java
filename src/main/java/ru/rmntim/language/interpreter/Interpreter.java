package ru.rmntim.language.interpreter;

import ru.rmntim.language.env.Environment;
import ru.rmntim.language.env.LoxCallable;
import ru.rmntim.language.env.LoxFunction;
import ru.rmntim.language.env.Variable;
import ru.rmntim.language.interpreter.expression.*;
import ru.rmntim.language.interpreter.statement.Class;
import ru.rmntim.language.interpreter.statement.*;
import ru.rmntim.language.token.Token;
import ru.rmntim.language.token.TokenType;
import ru.rmntim.language.util.ErrorReporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {
    private final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expression, Integer> locals = new HashMap<>();

    public Interpreter() {
        globals.define("time", new LoxCallable() {

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Variable> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("print", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Variable> arguments) {
                System.out.println(stringify(arguments.getFirst()));
                return null;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

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
        if (value instanceof Variable) {
            value = ((Variable) value).getValue();
        }

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
    public Object visit(Literal expression) {
        return expression.getValue();
    }

    @Override
    public Object visit(Grouping expression) {
        return evaluate(expression.getSubExpression());
    }

    @Override
    public Object visit(Unary expression) {
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
    public Object visit(Binary expression) {
        var left = evaluate(expression.getLeft());
        var right = evaluate(expression.getRight());

        if (left instanceof Variable) {
            left = ((Variable) left).getValue();
        }
        if (right instanceof Variable) {
            right = ((Variable) right).getValue();
        }

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
    public Object visit(ru.rmntim.language.interpreter.expression.Variable expression) {
        return lookUpVariable(expression.getName(), expression);
    }

    @Override
    public Object visit(Assignment expression) {
        var value = evaluate(expression.getValue());

        var distance = locals.get(expression);
        if (distance != null) {
            environment.assignAt(distance, expression.getName(), value);
        } else {
            globals.assign(expression.getName(), value);
        }

        return value;
    }

    @Override
    public Object visit(Ternary expression) {
        if (isTruthy(evaluate(expression.getCondition()))) {
            return evaluate(expression.getThenBranch());
        } else {
            return evaluate(expression.getElseBranch());
        }
    }

    @Override
    public Object visit(Logical expression) {
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
    public Object visit(Call expression) {
        var callee = evaluate(expression.getCalee());

        var arguments = new ArrayList<Variable>();
        for (var argument : expression.getArguments()) {
            arguments.add(new Variable(evaluate(argument)));
        }

        if (!(callee instanceof LoxCallable function)) {
            throw new RuntimeError(expression.getParen(),
                    "Call to a non-callable object");
        }

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expression.getParen(),
                    "Expected " + function.arity() +
                            " arguments but got " + arguments.size());
        }

        return function.call(this, arguments);
    }

    @Override
    public Void visit(Expr statement) {
        evaluate(statement.getExpression());
        return null;
    }

    @Override
    public Void visit(Let statement) {
        var variable = new Variable();

        if (statement.getInitializer() != null) {
            variable.setValue(evaluate(statement.getInitializer()));
            variable.setInitialized();
        }

        environment.define(statement.getName().literal(), variable);
        return null;
    }

    @Override
    public Void visit(Block statement) {
        executeBlock(statement.getStatements(), new Environment(environment));
        return null;
    }

    @Override
    public Void visit(If statement) {
        if (isTruthy(evaluate(statement.getCondition()))) {
            execute(statement.getThenBranch());
        } else if (statement.getElseBranch().isPresent()) {
            execute(statement.getElseBranch().get());
        }
        return null;
    }

    @Override
    public Void visit(While statement) {
        while (isTruthy(evaluate(statement.getCondition()))) {
            try {
                execute(statement.getBody());
            } catch (BreakException be) {
                break;
            }
        }
        return null;
    }

    @Override
    public Void visit(Break statement) {
        throw new BreakException();
    }

    @Override
    public Void visit(Function statement) {
        var function = new LoxFunction(statement, environment);
        environment.define(statement.getName().literal(), function);
        return null;
    }

    @Override
    public Void visit(Return statement) {
        var value = (statement.getValue().isPresent()) ? evaluate(statement.getValue().get()) : null;
        throw new ReturnException(value);
    }

    @Override
    public Void visit(Class statement) {
        environment.define(statement.getName().literal(), (LoxCallable) null);
        var class_ = new LoxClass(statement.getName().literal());
        environment.assign(statement.getName(), class_);
        return null;
    }

    public void executeBlock(List<Statement> statements, Environment environment) {
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
            if (operand instanceof Variable) {
                operand = ((Variable) operand).getValue();
            }
            if (!(operand instanceof Double)) {
                System.err.println("[DBG] " + operand);
                System.err.println(operand.getClass());
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

    public void resolve(Expression expression, int depth) {
        locals.put(expression, depth);
    }

    private Object lookUpVariable(Token name, Expression expression) {
        var distance = locals.get(expression);
        if (distance != null) {
            return environment.getAt(distance, name.literal());
        } else {
            return globals.get(name);
        }
    }
}
