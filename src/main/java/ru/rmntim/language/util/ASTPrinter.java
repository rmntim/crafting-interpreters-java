package ru.rmntim.language.util;

import ru.rmntim.language.Expression;

public class ASTPrinter implements Expression.Visitor<String> {
    public String print(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public String visit(Expression.Binary expression) {
        return parenthesize(expression.getOperator().literal(), expression.getLeft(), expression.getRight());
    }

    @Override
    public String visit(Expression.Unary expression) {
        return parenthesize(expression.getOperator().literal(), expression.getRight());
    }

    @Override
    public String visit(Expression.Grouping expression) {
        return parenthesize("group", expression.getSubExpression());
    }

    @Override
    public String visit(Expression.Literal expression) {
        return expression.getValue() == null ? "nil" : expression.getValue().toString();
    }

    private String parenthesize(String name, Expression... expressions) {
        var builder = new StringBuilder();

        builder.append("(").append(name);
        for (var expression : expressions) {
            builder.append(" ");
            builder.append(expression.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}
