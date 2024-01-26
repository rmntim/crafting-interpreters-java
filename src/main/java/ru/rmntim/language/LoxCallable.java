package ru.rmntim.language;

import java.util.List;

public interface LoxCallable {
    int arity();

    Object call(Interpreter interpreter, List<Variable> arguments);
}
