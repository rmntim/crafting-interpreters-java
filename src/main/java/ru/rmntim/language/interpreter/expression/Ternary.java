package ru.rmntim.language.interpreter.expression;

public class Ternary extends Expression {
    private final Expression condition;
    private final Expression thenBranch;
    private final Expression elseBranch;

    public Ternary(Expression condition, Expression thenBranch, Expression elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public Expression getElseBranch() {
        return elseBranch;
    }

    public Expression getThenBranch() {
        return thenBranch;
    }

    public Expression getCondition() {
        return condition;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
