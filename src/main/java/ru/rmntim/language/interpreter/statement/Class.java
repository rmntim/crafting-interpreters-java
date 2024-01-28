package ru.rmntim.language.interpreter.statement;

import ru.rmntim.language.interpreter.expression.Variable;
import ru.rmntim.language.token.Token;

import java.util.List;
import java.util.Optional;

public class Class extends Statement {
    private final Token name;
    private final Variable superclass;
    private final List<Function> methods;

    public Class(Token name, Variable superclass, List<Function> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    public Token getName() {
        return name;
    }

    public Optional<Variable> getSuperclass() {
        return Optional.ofNullable(superclass);
    }

    public List<Function> getMethods() {
        return methods;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
