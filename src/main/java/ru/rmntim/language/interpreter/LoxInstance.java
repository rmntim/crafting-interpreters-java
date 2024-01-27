package ru.rmntim.language.interpreter;

import ru.rmntim.language.token.Token;

import java.util.Map;

public record LoxInstance(LoxClass class_, Map<String, Object> fields) {
    public Object get(Token name) {
        if (fields.containsKey(name.literal())) {
            return fields.get(name.literal());
        }

        throw new RuntimeError(name,
                "Undefined property '" + name.literal() + "'");
    }

    @Override
    public String toString() {
        return class_.name() + " instance";
    }

    public void set(Token name, Object value) {
        fields.put(name.literal(), value);
    }
}
