package ru.rmntim.language;

import ru.rmntim.language.token.Token;
import ru.rmntim.language.token.TokenType;
import ru.rmntim.language.util.Logger;

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

    public Expression parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expression expression() {
        return equality();
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
                case VAR:
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
        Logger.error(token, message);
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
