package ru.rmntim.language.interpreter.statement;

public abstract class Statement {
    public interface Visitor<T> {
        T visit(Expr statement);

        T visit(Let statement);

        T visit(Block statement);

        T visit(If statement);

        T visit(While statement);

        T visit(Break statement);

        T visit(Function statement);

        T visit(Return statement);
    }

    public abstract <T> T accept(Visitor<T> visitor);
}
