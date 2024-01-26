package ru.rmntim.language.interpreter.statement;

import ru.rmntim.language.interpreter.expression.Expression;
import ru.rmntim.language.token.Token;

import java.util.Optional;

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

    public Optional<Expression> getValue() {
        return Optional.ofNullable(value);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
