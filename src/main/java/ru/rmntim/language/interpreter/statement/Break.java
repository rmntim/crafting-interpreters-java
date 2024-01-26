package ru.rmntim.language.interpreter.statement;

public class Break extends Statement {
    public Break() {
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
