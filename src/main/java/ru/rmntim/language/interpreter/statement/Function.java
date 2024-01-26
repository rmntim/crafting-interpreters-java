package ru.rmntim.language.interpreter.statement;

import ru.rmntim.language.token.Token;

import java.util.List;

public class Function extends Statement {
    private final Token name;
    private final List<Token> params;
    private final List<Statement> body;

    public Function(Token name, List<Token> params, List<Statement> body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }

    public Token getName() {
        return name;
    }

    public List<Token> getParams() {
        return params;
    }

    public List<Statement> getBody() {
        return body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
