package ru.rmntim.language.env;

import ru.rmntim.language.interpreter.Interpreter;
import ru.rmntim.language.interpreter.ReturnException;
import ru.rmntim.language.interpreter.statement.Function;

import java.util.List;

public record LoxFunction(Function declaration, Environment closure) implements LoxCallable {

    @Override
    public int arity() {
        return declaration.getParams().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Variable> arguments) {
        var environment = new Environment(closure);
        for (int i = 0; i < declaration.getParams().size(); ++i) {
            environment.define(declaration.getParams().get(i).literal(), arguments.get(i));
        }
        try {
            interpreter.executeBlock(declaration.getBody(), environment);
        } catch (ReturnException returnValue) {
            return returnValue.getValue();
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.getName().literal() + ">";
    }
}
