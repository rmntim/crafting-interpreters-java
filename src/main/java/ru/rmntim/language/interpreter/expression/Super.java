package ru.rmntim.language.interpreter.expression;

import ru.rmntim.language.token.Token;

public class Super extends Expression {
    private final Token keyword;
    private final Token method;

    public Super(Token keyword, Token method) {
        this.keyword = keyword;
        this.method = method;
    }

    public Token getKeyword() {
        return keyword;
    }

    public Token getMethod() {
        return method;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
