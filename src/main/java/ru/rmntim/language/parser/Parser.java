package ru.rmntim.language.parser;

import ru.rmntim.language.interpreter.expression.*;
import ru.rmntim.language.interpreter.statement.Class;
import ru.rmntim.language.interpreter.statement.*;
import ru.rmntim.language.token.Token;
import ru.rmntim.language.token.TokenType;
import ru.rmntim.language.util.ErrorReporter;

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
            if (expect(CLASS)) {
                return classDeclaration();
            }
            if (expect(FUNCTION)) {
                return function("function");
            }
            if (expect(LET)) {
                return letDeclaration();
            }
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Statement classDeclaration() {
        var name = consume(IDENTIFIER, "Expected class name");
        consume(LEFT_BRACE, "Expected '{' after class name");

        var methods = new ArrayList<Function>();
        while (!check(RIGHT_BRACE) && !isEnd()) {
            methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "Expected '}' after class body");
        return new Class(name, methods);
    }

    private Function function(String type) {
        var name = consume(IDENTIFIER, "Expected " + type + " name");

        consume(LEFT_PAREN, "Expected '(' after " + type + " name");
        var parameters = new ArrayList<Token>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 255) {
                    //noinspection ThrowableNotThrown
                    error(peek(), "Reached the limit of arguments (255)");
                }

                parameters.add(consume(IDENTIFIER, "Expected parameter name"));
            } while (expect(COMMA));
        }
        consume(RIGHT_PAREN, "Expected ')' after parameters");

        consume(LEFT_BRACE, "Expected '{' before " + type + " body");
        var body = block();
        return new Function(name, parameters, body);
    }

    private Statement letDeclaration() {
        var name = consume(IDENTIFIER, "Expected variable name");
        Expression initializer = null;

        if (expect(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expected ';' after variable declaration");
        return new Let(name, initializer);
    }

    private Statement statement() {
        if (expect(FOR)) {
            return forStatement();
        }
        if (expect(IF)) {
            return ifStatement();
        }
        if (expect(RETURN)) {
            return returnStatement();
        }
        if (expect(BREAK)) {
            return breakStatement();
        }
        if (expect(WHILE)) {
            return whileStatement();
        }
        if (expect(LEFT_BRACE)) {
            return new Block(block());
        }
        return expressionStatement();
    }

    private Statement returnStatement() {
        var keyword = previous();
        var value = check(SEMICOLON) ? null : expression();
        consume(SEMICOLON, "Expected ';' after return value");
        return new Return(keyword, value);
    }

    private Statement breakStatement() {
        var breakToken = previous();
        consume(SEMICOLON, "Expected ';' after 'break'");
        return new Break(breakToken);
    }

    private Statement forStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'for'");

        Statement initializer;
        if (expect(SEMICOLON)) {
            initializer = null;
        } else if (expect(LET)) {
            initializer = letDeclaration();
        } else {
            initializer = expressionStatement();
        }

        var condition = check(SEMICOLON) ? new Literal(true) : expression();
        consume(SEMICOLON, "Expected ';' after loop condition");

        var increment = check(RIGHT_PAREN) ? null : expression();
        consume(RIGHT_PAREN, "Expected ')' after 'for' clauses");

        var body = statement();

        if (increment != null) {
            body = new Block(List.of(body, new Expr(increment)));
        }

        body = new While(condition, body);

        if (initializer != null) {
            body = new Block(List.of(initializer, body));
        }

        return body;
    }

    private Statement whileStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'while'");
        var condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after while condition");

        var body = statement();

        return new While(condition, body);
    }

    private Statement ifStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'if'");
        var condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after if condition");

        var thenBranch = statement();
        var elseBranch = expect(ELSE) ? statement() : null;

        return new If(condition, thenBranch, elseBranch);
    }

    private List<Statement> block() {
        var statements = new ArrayList<Statement>();

        while (!check(RIGHT_BRACE) && !isEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expected '}' at the end of the block");
        // Method doesn't return the `Statement.Block` class,
        // so it can be reused for function bodies.
        return statements;
    }

    private Statement expressionStatement() {
        var expr = expression();
        consume(SEMICOLON, "Expected ';' after expression");
        return new Expr(expr);
    }

    private Expression expression() {
        var expr = assignment();

        if (expect(QUESTION)) {
            var question = previous();
            var thenBranch = expression();

            if (expect(COLON)) {
                var elseBranch = expression();
                return new Ternary(expr, thenBranch, elseBranch);
            }
            throw error(question, "Expected else branch for ternary expression");
        }

        return expr;
    }

    private Expression assignment() {
        var expr = or();

        if (expect(EQUAL)) {
            var equals = previous();
            var value = assignment();

            if (expr instanceof Variable) {
                var name = ((Variable) expr).getName();
                return new Assignment(name, value);
            } else if (expr instanceof Get get) {
                return new Set(get.getObject(), get.getName(), value);
            }

            // Not throwing the error, because parser technically is in right state, so nothing is broken
            //noinspection ThrowableNotThrown
            error(equals, "Invalid assignment target");
        }

        return expr;
    }

    private Expression or() {
        var expr = and();

        while (expect(OR)) {
            var operator = previous();
            var right = and();
            expr = new Logical(expr, operator, right);
        }

        return expr;
    }

    private Expression and() {
        var expr = equality();

        while (expect(AND)) {
            var operator = previous();
            var right = equality();
            expr = new Logical(expr, operator, right);
        }

        return expr;
    }

    private Expression equality() {
        var expr = comparison();

        while (expect(BANG_EQUAL, EQUAL_EQUAL)) {
            var operator = previous();
            var right = comparison();
            expr = new Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression comparison() {
        var expr = term();

        while (expect(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            var operator = previous();
            var right = term();
            expr = new Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression term() {
        var expr = factor();

        while (expect(MINUS, PLUS)) {
            var operator = previous();
            var right = factor();
            expr = new Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression factor() {
        var expr = unary();

        while (expect(SLASH, STAR)) {
            var operator = previous();
            var right = unary();
            expr = new Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression unary() {
        if (expect(BANG, MINUS)) {
            var operator = previous();
            var right = unary();
            return new Unary(operator, right);
        }

        return call();
    }

    private Expression call() {
        var expr = primary();

        while (true) {
            if (expect(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (expect(DOT)) {
                var name = consume(IDENTIFIER, "Expected property name after '.'");
                expr = new Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expression finishCall(Expression callee) {
        var arguments = new ArrayList<Expression>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    //noinspection ThrowableNotThrown
                    error(peek(), "Reached the limit of arguments (255)");
                }
                arguments.add(expression());
            } while (expect(COMMA));
        }

        var paren = consume(RIGHT_PAREN, "Expected ')' after arguments to a function call");

        return new Call(callee, paren, arguments);
    }

    private Expression primary() {
        if (expect(FALSE)) return new Literal(false);
        if (expect(TRUE)) return new Literal(true);
        if (expect(NIL)) return new Literal(null);

        if (expect(NUMBER, STRING)) {
            return new Literal(previous().value());
        }

        if (expect(SELF)) {
            return new Self(previous());
        }

        if (expect(IDENTIFIER)) {
            return new Variable(previous());
        }

        if (expect(LEFT_PAREN)) {
            var expr = expression();
            consume(RIGHT_PAREN, "Expected ')' after expression");
            return new Grouping(expr);
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
