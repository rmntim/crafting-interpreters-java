package ru.rmntim.language.token;

public record Token(TokenType type, String literal, Object value, int line) {
}
