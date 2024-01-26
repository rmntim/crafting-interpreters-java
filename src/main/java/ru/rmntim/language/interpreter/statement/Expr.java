package ru.rmntim.language.interpreter.statement;

import ru.rmntim.language.interpreter.expression.Expression;

public class Expr extends Statement {
    private final Expression expression;

    public Expr(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
