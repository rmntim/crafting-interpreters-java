package ru.rmntim.language.interpreter;

import ru.rmntim.language.env.LoxCallable;
import ru.rmntim.language.env.Variable;

import java.util.List;

public record LoxClass(String name) implements LoxCallable {
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Variable> arguments) {
        var instance = new LoxInstance(this);
        return instance;
    }
}
