package ru.rmntim.language.interpreter;

public class ReturnException extends RuntimeException {
    private final Object value;

    public ReturnException(Object value) {
        super(null, null, false, false);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
