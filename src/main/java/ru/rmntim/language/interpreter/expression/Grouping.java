package ru.rmntim.language.interpreter.expression;

public class Grouping extends Expression {
    private final Expression subExpression;

    public Grouping(Expression expression) {
        this.subExpression = expression;
    }

    public Expression getSubExpression() {
        return subExpression;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
