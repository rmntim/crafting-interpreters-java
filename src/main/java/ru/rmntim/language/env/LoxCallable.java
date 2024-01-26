package ru.rmntim.language.env;

import ru.rmntim.language.interpreter.Interpreter;

import java.util.List;

public interface LoxCallable {
    int arity();

    Object call(Interpreter interpreter, List<Variable> arguments);
}
