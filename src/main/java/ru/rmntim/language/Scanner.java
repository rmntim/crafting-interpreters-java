package ru.rmntim.language;

import ru.rmntim.language.token.Token;
import ru.rmntim.language.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.rmntim.language.token.TokenType.*;

public class Scanner {
    private final String source;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fn", FUNCTION);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("let", LET);
        keywords.put("while", WHILE);
        keywords.put("break", BREAK);
    }

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isEof()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        var c = next();
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '?':
                addToken(QUESTION);
                break;
            case ':':
                addToken(COLON);
                break;
            case '!':
                addToken(expect('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(expect('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(expect('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(expect('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (expect('/')) {
                    while (peek() != '\n' && !isEof()) {
                        next();
                    }
                } else if (expect('*')) {
                    readBlockComment();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"':
                readString();
                break;
            default:
                if (isDigit(c)) {
                    readNumber();
                } else if (isAlpha(c)) {
                    readIdentifier();
                } else {
                    ErrorReporter.error(line, "Unexpected character");
                }
                break;
        }
    }

    private void readBlockComment() {
        while (peek() != '*' && peekNext() != '/' && !isEof()) {
            if (peek() == '\n') {
                line++;
            }
            next();
        }

        if (isEof()) {
            return;
        }

        next();
        next();
    }

    private void readIdentifier() {
        while (isAlphaNumeric(peek())) {
            next();
        }

        var text = source.substring(start, current);
        var type = keywords.getOrDefault(text, IDENTIFIER);
        addToken(type);
    }

    private void readNumber() {
        while (Character.isDigit(peek())) {
            next();
        }

        if (peek() == '.' && Character.isDigit(peekNext())) {
            next();
            while (Character.isDigit(peek())) {
                next();
            }
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void readString() {
        while (peek() != '"' && !isEof()) {
            if (peek() == '\n') {
                line++;
            }
            next();
        }

        if (isEof()) {
            ErrorReporter.error(line, "Unterminated string");
            return;
        }

        next();

        var value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private char peek() {
        if (isEof()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }


    private boolean expect(char expected) {
        if (isEof() || (source.charAt(current) != expected)) {
            return false;
        }
        current++;
        return true;
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object value) {
        var text = source.substring(start, current);
        tokens.add(new Token(type, text, value, line));
    }

    private char next() {
        return source.charAt(current++);
    }

    private boolean isEof() {
        return current >= source.length();
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
