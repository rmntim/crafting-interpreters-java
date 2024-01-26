package ru.rmntim.language.interpreter.statement;

import ru.rmntim.language.interpreter.expression.Expression;
import ru.rmntim.language.token.Token;

public class Let extends Statement {
    private final Token name;
    private final Expression initializer;

    public Let(Token name, Expression initializer) {
        this.name = name;
        this.initializer = initializer;
    }

    public Token getName() {
        return name;
    }

    public Expression getInitializer() {
        return initializer;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
