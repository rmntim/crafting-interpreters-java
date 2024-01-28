package ru.rmntim.language.resolver;

import ru.rmntim.language.interpreter.Interpreter;
import ru.rmntim.language.interpreter.expression.*;
import ru.rmntim.language.interpreter.statement.Class;
import ru.rmntim.language.interpreter.statement.*;
import ru.rmntim.language.token.Token;
import ru.rmntim.language.util.ErrorReporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;
    private boolean inLoop = false;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public Void visit(Binary expression) {
        resolve(expression.getLeft());
        resolve(expression.getRight());
        return null;
    }

    @Override
    public Void visit(Unary expression) {
        resolve(expression.getRight());
        return null;
    }

    @Override
    public Void visit(Grouping expression) {
        resolve(expression.getSubExpression());
        return null;
    }

    @Override
    public Void visit(Literal expression) {
        return null;
    }

    @Override
    public Void visit(Variable expression) {
        if (!scopes.isEmpty() &&
                scopes.peek().get(expression.getName().literal()) == Boolean.FALSE) {
            ErrorReporter.error(expression.getName(),
                    "Unable to read local variable in its own initializer");
        }
        resolveLocal(expression, expression.getName());
        return null;
    }

    @Override
    public Void visit(Assignment expression) {
        resolve(expression.getValue());
        resolveLocal(expression, expression.getName());
        return null;
    }

    @Override
    public Void visit(Ternary expression) {
        resolve(expression.getCondition());
        resolve(expression.getThenBranch());
        resolve(expression.getElseBranch());
        return null;
    }

    @Override
    public Void visit(Logical expression) {
        resolve(expression.getLeft());
        resolve(expression.getRight());
        return null;
    }

    @Override
    public Void visit(Call expression) {
        resolve(expression.getCalee());
        for (var argument : expression.getArguments()) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visit(Get expression) {
        resolve(expression.getObject());
        return null;
    }

    @Override
    public Void visit(Set expression) {
        resolve(expression.getValue());
        resolve(expression.getObject());
        return null;
    }

    @Override
    public Void visit(Self expression) {
        if (currentClass == ClassType.NONE) {
            ErrorReporter.error(expression.getKeyword(),
                    "'self' outside of classes is not allowed");
            return null;
        }
        resolveLocal(expression, expression.getKeyword());
        return null;
    }

    @Override
    public Void visit(Expr statement) {
        resolve(statement.getExpression());
        return null;
    }

    @Override
    public Void visit(Let statement) {
        declare(statement.getName());
        if (statement.getInitializer() != null) {
            resolve(statement.getInitializer());
        }
        define(statement.getName());
        return null;
    }

    @Override
    public Void visit(Block statement) {
        beginScope();
        resolve(statement.getStatements());
        endScope();
        return null;
    }

    @Override
    public Void visit(If statement) {
        resolve(statement.getCondition());
        resolve(statement.getThenBranch());
        statement.getElseBranch().ifPresent(this::resolve);
        return null;
    }

    @Override
    public Void visit(While statement) {
        inLoop = true;
        resolve(statement.getCondition());
        resolve(statement.getBody());
        inLoop = false;
        return null;
    }

    @Override
    public Void visit(Break statement) {
        if (!inLoop) {
            ErrorReporter.error(statement.getName(), "`break` only allowed in loops");
        }
        return null;
    }

    @Override
    public Void visit(Function statement) {
        declare(statement.getName());
        define(statement.getName());
        resolveFunction(statement, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visit(Return statement) {
        if (currentFunction == FunctionType.NONE) {
            ErrorReporter.error(statement.getKeyword(), "Unable to return at the top-level");
        }
        if (statement.getValue().isPresent()) {
            if (currentFunction == FunctionType.CONSTRUCTOR) {
                ErrorReporter.error(statement.getKeyword(),
                        "Unable to return from a constructor");
            }
        }
        resolve(statement.getValue().get());
        return null;
    }

    @Override
    public Void visit(Class statement) {
        var enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
        declare(statement.getName());
        define(statement.getName());

        beginScope();
        scopes.peek().put("self", true);

        for (var method : statement.getMethods()) {
            var declaration = FunctionType.METHOD;
            if (method.getName().literal().equals("init")) {
                declaration = FunctionType.CONSTRUCTOR;
            }
            resolveFunction(method, declaration);
        }

        endScope();

        currentClass = enclosingClass;
        return null;
    }

    public void resolve(List<Statement> statements) {
        for (var statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Statement statement) {
        statement.accept(this);
    }

    private void resolve(Expression expression) {
        expression.accept(this);
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        var scope = scopes.peek();
        if (scope.containsKey(name.literal())) {
            ErrorReporter.error(name, "Variable with this name already exists in scope");
        }
        scope.put(name.literal(), false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) {
            return;
        }
        scopes.peek().put(name.literal(), true);
    }

    private void resolveLocal(Expression expression, Token name) {
        for (int i = scopes.size() - 1; i >= 0; --i) {
            if (scopes.get(i).containsKey(name.literal())) {
                interpreter.resolve(expression, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveFunction(Function function, FunctionType type) {
        var enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (var param : function.getParams()) {
            declare(param);
            define(param);
        }
        resolve(function.getBody());
        endScope();
        currentFunction = enclosingFunction;
    }
}
