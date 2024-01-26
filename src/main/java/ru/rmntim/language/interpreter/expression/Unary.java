package ru.rmntim.language.interpreter.expression;

import ru.rmntim.language.token.Token;

public class Unary extends Expression {
    private final Token operator;
    private final Expression right;

    public Unary(Token operator, Expression right) {
        this.operator = operator;
        this.right = right;
    }

    public Token getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
