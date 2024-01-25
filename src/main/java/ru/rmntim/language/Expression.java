package ru.rmntim.language;

import ru.rmntim.language.token.Token;

import java.util.List;

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
    }

    public abstract <T> T accept(Visitor<T> visitor);

    public static class Binary extends Expression {
        private final Expression left;
        private final Token operator;
        private final Expression right;

        public Binary(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

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
        private final Expression subExpression;

        public Grouping(Expression expression) {
            this.subExpression = expression;
        }

        public Expression getSubExpression() {
            return subExpression;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Literal extends Expression {
        private final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Unary extends Expression {
        private final Token operator;
        private final Expression right;

        public Unary(Token operator, Expression right) {
            this.operator = operator;
            this.right = right;
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

    public static class Variable extends Expression {
        private final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        public Token getName() {
            return name;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Assignment extends Expression {
        private final Token name;
        private final Expression value;

        public Assignment(Token name, Expression value) {
            this.name = name;
            this.value = value;
        }

        public Token getName() {
            return name;
        }

        public Expression getValue() {
            return value;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Ternary extends Expression {
        private final Expression condition;
        private final Expression thenBranch;
        private final Expression elseBranch;

        public Ternary(Expression condition, Expression thenBranch, Expression elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        public Expression getElseBranch() {
            return elseBranch;
        }

        public Expression getThenBranch() {
            return thenBranch;
        }

        public Expression getCondition() {
            return condition;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Logical extends Expression {
        private final Expression left;
        private final Token operator;
        private final Expression right;

        public Logical(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public Expression getRight() {
            return right;
        }

        public Token getOperator() {
            return operator;
        }

        public Expression getLeft() {
            return left;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }

    public static class Call extends Expression {
        private final Expression calee;
        private final Token paren;
        private final List<Expression> arguments;

        public Call(Expression calee, Token paren, List<Expression> arguments) {
            this.calee = calee;
            this.paren = paren;
            this.arguments = arguments;
        }

        public Expression getCalee() {
            return calee;
        }

        public Token getParen() {
            return paren;
        }

        public List<Expression> getArguments() {
            return arguments;
        }

        @Override
        public <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }
    }
}
