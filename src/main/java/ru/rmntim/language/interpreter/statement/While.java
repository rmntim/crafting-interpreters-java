package ru.rmntim.language.interpreter.statement;

import ru.rmntim.language.interpreter.expression.Expression;

public class While extends Statement {
    private final Expression condition;
    private final Statement body;

    public While(Expression condition, Statement body) {
        this.condition = condition;
        this.body = body;
    }

    public Expression getCondition() {
        return condition;
    }

    public Statement getBody() {
        return body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
