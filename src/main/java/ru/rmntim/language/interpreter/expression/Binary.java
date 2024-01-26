package ru.rmntim.language.interpreter.expression;

import ru.rmntim.language.token.Token;

public class Binary extends Expression {
    private final Expression left;
    private final Token operator;
    private final Expression right;

    public Binary(Expression left, Token operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
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
