package ru.rmntim.language;

import ru.rmntim.language.token.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();
    private final Environment parent;

    public Environment() {
        parent = null;
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object get(Token name) {
        if (values.containsKey(name.literal())) {
            return values.get(name.literal());
        }

        if (parent != null) {
            return parent.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.literal() + "'");
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.literal())) {
            values.put(name.literal(), value);
            return;
        }

        if (parent != null) {
            parent.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.literal() + "'");
    }
}
