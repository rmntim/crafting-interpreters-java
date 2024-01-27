package ru.rmntim.language.interpreter.expression;

import ru.rmntim.language.token.Token;

public class Get extends Expression {
    private final Expression object;
    private final Token name;

    public Get(Expression object, Token name) {
        this.object = object;
        this.name = name;
    }

    public Expression getObject() {
        return object;
    }

    public Token getName() {
        return name;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
