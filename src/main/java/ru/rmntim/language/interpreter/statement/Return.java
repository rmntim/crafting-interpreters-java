package ru.rmntim.language.interpreter.statement;

import ru.rmntim.language.interpreter.expression.Expression;
import ru.rmntim.language.token.Token;

public class Return extends Statement {
    private final Token keyword;
    private final Expression value;

    public Return(Token keyword, Expression value) {
        this.keyword = keyword;
        this.value = value;
    }

    public Token getKeyword() {
        return keyword;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
