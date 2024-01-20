package ru.rmntim.language;

import ru.rmntim.language.token.Token;

public abstract class Statement {
    public interface Visitor<T> {
        T visit(Expr statement);

        T visit(Print statement);

        T visit(Let statement);
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
}
