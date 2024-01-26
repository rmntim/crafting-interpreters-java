package ru.rmntim.language.interpreter.expression;

import ru.rmntim.language.token.Token;

public class Assignment extends Expression {
    private final Token name;
    private final Expression value;

    public Assignment(Token name, Expression value) {
        this.name = name;
        this.value = value;
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
