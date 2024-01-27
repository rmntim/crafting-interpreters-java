package ru.rmntim.language.interpreter.statement;

import ru.rmntim.language.token.Token;

import java.util.List;

public class Class extends Statement {
    private final Token name;
    private final List<Function> methods;

    public Class(Token name, List<Function> methods) {
        this.name = name;
        this.methods = methods;
    }

    public Token getName() {
        return name;
    }

    public List<Function> getMethods() {
        return methods;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
