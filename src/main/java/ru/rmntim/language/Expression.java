package ru.rmntim.language;

import ru.rmntim.language.token.Token;

public abstract class Expression {
    public interface Visitor<T> {
        T visit(Binary expression);

        T visit(Unary expression);

        T visit(Grouping expression);

        T visit(Literal expression);
    }

    public abstract <T> T accept(Visitor<T> visitor);

    public static class Binary extends Expression {
        public Binary(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        private final Expression left;
        private final Token operator;
        private final Expression right;

        public Expression getLeft() {
            return left;
        }

        public Token getOperator() {
            return operator;
        }

        public Expression getRight() {
            return right;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Grouping extends Expression {
        public Grouping(Expression expression) {
            this.subExpression = expression;
        }

        private final Expression subExpression;

        public Expression getSubExpression() {
            return subExpression;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Literal extends Expression {
        public Literal(Object value) {
            this.value = value;
        }

        private final Object value;

        public Object getValue() {
            return value;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Unary extends Expression {
        public Unary(Token operator, Expression right) {
            this.operator = operator;
            this.right = right;
        }

        private final Token operator;
        private final Expression right;

        public Token getOperator() {
            return operator;
        }

        public Expression getRight() {
            return right;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}
