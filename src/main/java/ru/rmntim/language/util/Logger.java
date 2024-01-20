package ru.rmntim.language.util;

import ru.rmntim.language.token.Token;
import ru.rmntim.language.token.TokenType;

public class Logger {
    public static boolean errorState = false;

    private Logger() {
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    public static void error(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), " at '" + token.literal() + "'", message);
        }
    }

    public static void report(int line, String location, String message) {
        System.err.println("[ERROR] (" + line + ")" + location + ": " + message);
        errorState = true;
    }
}
