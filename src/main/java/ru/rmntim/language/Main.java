package ru.rmntim.language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.err.println("Usage: lox [FILENAME]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runRepl();
        }
    }

    private static void runRepl() throws IOException {
        var inputReader = new InputStreamReader(System.in);
        var reader = new BufferedReader(inputReader);

        for (; ; ) {
            System.out.print("> ");
            var line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
            ErrorReporter.errorState = false;
        }
    }


    private static void runFile(String path) throws IOException {
        var bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (ErrorReporter.errorState) {
            System.exit(65);
        }
        if (ErrorReporter.runtimeErrorState) {
            System.exit(70);
        }
    }

    private static void run(String source) {
        var scanner = new Scanner(source);
        var tokens = scanner.scanTokens();
        var parser = new Parser(tokens);
        var expr = parser.parse();

        // Stop if there was a syntax error
        if (ErrorReporter.errorState) {
            return;
        }

        interpreter.interpret(expr);
    }
}