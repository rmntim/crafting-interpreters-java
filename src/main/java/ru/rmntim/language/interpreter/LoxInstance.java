package ru.rmntim.language.interpreter;

import ru.rmntim.language.token.Token;

import java.util.HashMap;
import java.util.Map;

public final class LoxInstance {
    private final LoxClass class_;
    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(LoxClass class_) {
        this.class_ = class_;
    }

    public Object get(Token name) {
        if (fields.containsKey(name.literal())) {
            return fields.get(name.literal());
        }

        var method = class_.findMethod(name.literal());
        if (method.isPresent()) {
            return method.get();
        }

        throw new RuntimeError(name,
                "Undefined property '" + name.literal() + "'");
    }

    public void set(Token name, Object value) {
        fields.put(name.literal(), value);
    }

    @Override
    public String toString() {
        return class_.name() + " instance";
    }
}
