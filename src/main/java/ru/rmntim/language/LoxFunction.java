package ru.rmntim.language;

import java.util.List;

public record LoxFunction(Statement.Function declaration) implements LoxCallable {

    @Override
    public int arity() {
        return declaration.getParams().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Variable> arguments) {
        var environment = new Environment(interpreter.getGlobals());
        for (int i = 0; i < declaration.getParams().size(); ++i) {
            environment.define(declaration.getParams().get(i).literal(), arguments.get(i));
        }

        interpreter.executeBlock(declaration.getBody(), environment);
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.getName().literal() + ">";
    }
}