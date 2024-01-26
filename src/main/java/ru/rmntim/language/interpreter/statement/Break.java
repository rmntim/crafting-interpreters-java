package ru.rmntim.language.interpreter.statement;

import ru.rmntim.language.token.Token;

public class Break extends Statement {
    private final Token name;

    public Break(Token name) {
        this.name = name;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public Token getName() {
        return name;
    }
}
