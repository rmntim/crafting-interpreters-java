package ru.rmntim.language;

import ru.rmntim.language.token.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Variable> variables = new HashMap<>();
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

    public Object get(Token name) {
        if (variables.containsKey(name.literal())) {
            var variable = variables.get(name.literal());
            if (variable.isInitialized()) {
                return variable.getValue();
            }

            throw new RuntimeError(name, "Uninitialized variable '" + name.literal() + "'");
        }

        if (parent != null) {
            return parent.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.literal() + "'");
    }

    public void assign(Token name, Object newValue) {
        if (variables.containsKey(name.literal())) {
            var value = variables.get(name.literal());
            value.setValue(newValue);
            value.setInitialized();
            return;
        }

        if (parent != null) {
            parent.assign(name, newValue);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.literal() + "'");
    }
}
