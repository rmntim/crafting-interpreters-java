package ru.rmntim.language;

public class Variable {
    private Object value;
    private boolean initialized;

    public Variable() {
        this.value = null;
        this.initialized = false;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized() {
        this.initialized = true;
    }
}
