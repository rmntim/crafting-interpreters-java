package ru.rmntim.language;

import ru.rmntim.language.token.Token;

import java.util.List;

public abstract class Statement {
    public interface Visitor<T> {
        T visit(Expr statement);

        T visit(Print statement);

        T visit(Let statement);

        T visit(Block statement);

        T visit(If statement);
    }

    public abstract <T> T accept(Visitor<T> visitor);

    public static class Expr extends Statement {
        private final Expression expression;

        public Expr(Expression expression) {
            this.expression = expression;
        }

        public Expression getExpression() {
            return expression;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Print extends Statement {
        private final Expression expression;

        public Print(Expression expression) {
            this.expression = expression;
        }

        public Expression getExpression() {
            return expression;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Let extends Statement {
        private final Token name;
        private final Expression initializer;

        public Let(Token name, Expression initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        public Token getName() {
            return name;
        }

        public Expression getInitializer() {
            return initializer;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Block extends Statement {
        private final List<Statement> statements;

        public Block(List<Statement> statements) {
            this.statements = statements;
        }

        public List<Statement> getStatements() {
            return statements;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class If extends Statement {
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

        public Statement getElseBranch() {
            return elseBranch;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}
