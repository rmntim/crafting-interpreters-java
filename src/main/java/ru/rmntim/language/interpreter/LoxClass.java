package ru.rmntim.language.interpreter;

import ru.rmntim.language.env.LoxCallable;
import ru.rmntim.language.env.LoxFunction;
import ru.rmntim.language.env.Variable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) implements LoxCallable {
    public Optional<LoxFunction> findMethod(String name) {
        if (methods.containsKey(name)) {
            return Optional.of(methods.get(name));
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        return findMethod("init").map(LoxFunction::arity).orElse(0);
    }

    @Override
    public Object call(Interpreter interpreter, List<Variable> arguments) {
        var instance = new LoxInstance(this);
        var constructor = findMethod("init");
        constructor.ifPresent(init -> init.bind(instance).call(interpreter, arguments));
        return instance;
    }
}
