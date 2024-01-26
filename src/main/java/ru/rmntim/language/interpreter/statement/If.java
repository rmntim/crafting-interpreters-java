package ru.rmntim.language.interpreter.statement;

import ru.rmntim.language.interpreter.expression.Expression;

import java.util.Optional;

public class If extends Statement {
    private final Expression condition;
    private final Statement thenBranch;
    private final Statement elseBranch;

    public If(Expression condition, Statement thenBranch, Statement elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public Expression getCondition() {
        return condition;
    }

    public Statement getThenBranch() {
        return thenBranch;
    }

    public Optional<Statement> getElseBranch() {
        return Optional.ofNullable(elseBranch);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
