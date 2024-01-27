package ru.rmntim.language.interpreter.expression;

import ru.rmntim.language.token.Token;

public class Set extends Expression {
    private final Expression object;
    private final Token name;
    private final Expression value;

    public Set(Expression object, Token name, Expression value) {
        this.object = object;
        this.name = name;
        this.value = value;
    }

    public Expression getObject() {
        return object;
    }

    public Token getName() {
        return name;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
