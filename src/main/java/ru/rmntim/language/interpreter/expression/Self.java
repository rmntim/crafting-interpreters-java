package ru.rmntim.language.interpreter.expression;

import ru.rmntim.language.token.Token;

public class Self extends Expression {
    private final Token keyword;

    public Self(Token keyword) {
        this.keyword = keyword;
    }

    public Token getKeyword() {
        return keyword;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
