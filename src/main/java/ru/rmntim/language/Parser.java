package ru.rmntim.language;

import ru.rmntim.language.token.Token;
import ru.rmntim.language.token.TokenType;

import java.util.ArrayList;
import java.util.List;

import static ru.rmntim.language.token.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(final List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Statement> parse() {
        var statements = new ArrayList<Statement>();
        while (!isEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Statement declaration() {
        try {
            if (expect(LET)) {
                return letDeclaration();
            }
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Statement letDeclaration() {
        var name = consume(IDENTIFIER, "Expected variable name");
        Expression initializer = null;

        if (expect(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expected ';' after variable declaration");
        return new Statement.Let(name, initializer);
    }

    private Statement statement() {
        if (expect(PRINT)) {
            return printStatement();
        }
        return expressionStatement();
    }

    private Statement expressionStatement() {
        var expr = expression();
        consume(SEMICOLON, "Expected ';' after expression");
        return new Statement.Expr(expr);
    }

    private Statement printStatement() {
        var expr = expression();
        consume(SEMICOLON, "Expected ';' after expression");
        return new Statement.Print(expr);
    }

    private Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        var expr = equality();

        if (expect(EQUAL)) {
            var equals = previous();
            var value = assignment();

            if (expr instanceof Expression.Variable) {
                var name = ((Expression.Variable) expr).getName();
                return new Expression.Assignment(name, value);
            }

            // Not throwing the error, because parser technically is in right state, so nothing is broken
            error(equals, "Invalid assignment target");
        }

        return expr;
    }

    private Expression equality() {
        var expr = comparison();

        while (expect(BANG_EQUAL, EQUAL_EQUAL)) {
            var operator = previous();
            var right = comparison();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression comparison() {
        var expr = term();

        while (expect(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            var operator = previous();
            var right = term();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression term() {
        var expr = factor();

        while (expect(MINUS, PLUS)) {
            var operator = previous();
            var right = factor();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression factor() {
        var expr = unary();

        while (expect(SLASH, STAR)) {
            var operator = previous();
            var right = unary();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression unary() {
        if (expect(BANG, MINUS)) {
            var operator = previous();
            var right = unary();
            return new Expression.Unary(operator, right);
        }

        return primary();
    }

    private Expression primary() {
        if (expect(FALSE)) return new Expression.Literal(false);
        if (expect(TRUE)) return new Expression.Literal(true);
        if (expect(NIL)) return new Expression.Literal(null);

        if (expect(NUMBER, STRING)) {
            return new Expression.Literal(previous().value());
        }

        if (expect(IDENTIFIER)) {
            return new Expression.Variable(previous());
        }

        if (expect(LEFT_PAREN)) {
            var expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression");
            return new Expression.Grouping(expr);
        }

        throw error(peek(), "Expected expression");
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return next();
        }
        throw error(peek(), message);
    }

    private void synchronize() {
        next();

        while (!isEnd()) {
            if (previous().type() == SEMICOLON) {
                return;
            }

            switch (peek().type()) {
                case CLASS:
                case FUNCTION:
                case LET:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            next();
        }
    }

    private ParseError error(Token token, String message) {
        ErrorReporter.error(token, message);
        return new ParseError();
    }


    private boolean expect(TokenType... types) {
        for (var type : types) {
            if (check(type)) {
                next();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isEnd()) {
            return false;
        }

        return peek().type() == type;
    }

    private Token next() {
        if (!isEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isEnd() {
        return peek().type() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
