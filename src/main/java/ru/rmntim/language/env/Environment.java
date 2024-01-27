package ru.rmntim.language.env;

import ru.rmntim.language.interpreter.RuntimeError;
import ru.rmntim.language.token.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> variables = new HashMap<>();
    private final Environment parent;

    public Environment() {
        parent = null;
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void define(String name, Variable value) {
        variables.put(name, value);
    }

    public void define(String name, LoxCallable callable) {
        variables.put(name, callable);
    }

    public Object get(Token name) {
        if (variables.containsKey(name.literal())) {
            var value = variables.get(name.literal());
            if (value instanceof Variable variable) {
                if (variable.isInitialized()) {
                    return variable.getValue();
                }

                throw new RuntimeError(name, "Uninitialized variable '" + name.literal() + "'");
            } else if (value instanceof LoxCallable callable) {
                return callable;
            }
        }

        if (parent != null) {
            return parent.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.literal() + "'");
    }

    public Object getAt(int distance, String name) {
        return ancestor(distance).variables.get(name);
    }

    private Environment ancestor(int distance) {
        var environment = this;
        for (int i = 0; i < distance; ++i) {
            environment = environment.parent;
        }
        return environment;
    }

    public void assign(Token name, Object newValue) {
        if (variables.containsKey(name.literal())) {
            var value = variables.get(name.literal());
            if (value instanceof Variable variable) {
                variable.setValue(newValue);
                variable.setInitialized();
                return;
            } else if (value instanceof LoxCallable) {
                variables.put(name.literal(), newValue);
                return;
            }
        }

        if (parent != null) {
            parent.assign(name, newValue);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.literal() + "'");
    }

    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).variables.put(name.literal(), value);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        this.variables.forEach((String key, Object value) -> {
            sb.append(key).append(" -> ").append(value).append('\n');
        });
        return sb.toString();
    }
}
