package ru.rmntim.language.interpreter.expression;

import ru.rmntim.language.token.Token;

public class Variable extends Expression {
    private final Token name;

    public Variable(Token name) {
        this.name = name;
    }

    public Token getName() {
        return name;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
