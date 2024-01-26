package ru.rmntim.language.interpreter.expression;

import ru.rmntim.language.token.Token;

import java.util.List;

public class Call extends Expression {
    private final Expression calee;
    private final Token paren;
    private final List<Expression> arguments;

    public Call(Expression calee, Token paren, List<Expression> arguments) {
        this.calee = calee;
        this.paren = paren;
        this.arguments = arguments;
    }

    public Expression getCalee() {
        return calee;
    }

    public Token getParen() {
        return paren;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
