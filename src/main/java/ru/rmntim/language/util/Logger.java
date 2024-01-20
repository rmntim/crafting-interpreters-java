package ru.rmntim.language.util;

import ru.rmntim.language.RuntimeError;
import ru.rmntim.language.token.Token;
import ru.rmntim.language.token.TokenType;

public class Logger {
    public static boolean errorState = false;
    public static boolean runtimeErrorState = false;

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

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n(" + error.getToken().line() + ")");
        runtimeErrorState = true;
    }
}
