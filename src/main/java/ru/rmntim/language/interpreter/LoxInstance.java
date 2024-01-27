package ru.rmntim.language.interpreter;

public record LoxInstance(LoxClass class_) {
    @Override
    public String toString() {
        return class_.name() + " instance";
    }
}
