package ru.rmntim.language;

public abstract class Statement {
    public static class Expr {
        private final Expression expression;

        public Expr(Expression expression) {
            this.expression = expression;
        }

        public Expression getExpression() {
            return expression;
        }
    }

    public static class Print {
        private final Expression expression;

        public Print(Expression expression) {
            this.expression = expression;
        }

        public Expression getExpression() {
            return expression;
        }
    }
}
