package ru.rmntim.language.interpreter.expression;

public abstract class Expression {
    public interface Visitor<T> {
        T visit(Binary expression);

        T visit(Unary expression);

        T visit(Grouping expression);

        T visit(Literal expression);

        T visit(Variable expression);

        T visit(Assignment expression);

        T visit(Ternary expression);

        T visit(Logical expression);

        T visit(Call expression);

        T visit(Get expression);

        T visit(Set expression);

        T visit(Self expression);
    }

    public abstract <T> T accept(Visitor<T> visitor);
}
