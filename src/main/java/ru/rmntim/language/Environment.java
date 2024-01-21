package ru.rmntim.language;

import ru.rmntim.language.token.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object get(Token name) {
        if (values.containsKey(name.literal())) {
            return values.get(name.literal());
        }

        throw new RuntimeError(name, "Undefined variable '" + name.literal() + "'");
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.literal())) {
            values.put(name.literal(), value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.literal() + "'");
    }
}
