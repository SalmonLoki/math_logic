import java.util.*;
import java.util.function.Supplier;

class Parser {
    static String deleteWhiteSpaces(String s) {
        return Arrays.stream(s.split("\\s")).reduce(String::concat).orElse(null);
    }

    static class Tree {
        final Tree left, right;
        final String sign;

        Tree(String sign, Tree left, Tree right) {
            this.sign = sign;
            this.left = left;
            this.right = right;
        }

        private String string = null;

        @Override
        public String toString() {
            if (string == null) {
                string = (right == null || left == null ? "" : "(") +
                        (left == null ? "" : left.toString()) +
                        sign +
                        (right == null ? "" : right.toString()) +
                        (right == null || left == null ? "" : ")");
            }
            return string;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Tree && this.toString().equals(obj.toString());
        }
    }

    private enum Token {
        IMPL, OR, AND, NOT, BRACE_LEFT, BRACE_RIGHT, VARIABLE
    }

    Tree parse(String s) {
        str = deleteWhiteSpaces(s);
        index = 0;
        return expression(0);
    }

    class Operation {
        final Token token;
        final String sign;
        final boolean lefty;

        Operation(Token token, String sign, boolean lefty) {
            this.token = token;
            this.sign = sign;
            this.lefty = lefty;
        }
    }

    Operation[] operations = {
            new Operation(Token.IMPL, "->", true),
            new Operation(Token.OR, "|", false),
            new Operation(Token.AND, "&", false)
    };

    private Tree expression(int level) {
        if (level >= operations.length) return simple();

        Tree left = expression(level + 1);
        while (id == operations[level].token) {
            left = new Tree(operations[level].sign, left, operations[level].lefty ? expression(level) : expression(level + 1));
        }
        return left;
    }

    private Tree expression() {
        return expression(0);
    }

    String getVariable() {
        return variable.toString();
    }

    Map<Token, Supplier<Tree>> action = ((Supplier<Map<Token, Supplier<Tree>>>) () -> {
        Map<Token, Supplier<Tree>> t = new EnumMap<>(Token.class);
        t.put(Token.VARIABLE, () -> {
            Tree tt = new Tree(getVariable(), null, null);
            parseToken();
            return tt;
        });
        t.put(Token.NOT, () -> new Tree("!", null, simple()));
        t.put(Token.BRACE_LEFT, () -> {
            Tree tt = expression();
            parseToken();
            return tt;
        });
        return t;
    }).get();

    private Tree simple() {
        parseToken();
        return action.get(id).get();
    }

    Map<Character, Token> tokens = ((Supplier<Map<Character, Token>>) () -> {
        Map<Character, Token> t = new TreeMap<>();
        t.put('!', Token.NOT);
        t.put('|', Token.OR);
        t.put('&', Token.AND);
        t.put('-', Token.IMPL);
        t.put('(', Token.BRACE_LEFT);
        t.put(')', Token.BRACE_RIGHT);
        return t;
    }).get();

    private void parseToken() {
        char ch = nextChar();
        variable = new StringBuilder();
        if (Character.isAlphabetic(ch)) {
            while (Character.isDigit(ch) || Character.isAlphabetic(ch)) {
                variable.append(ch);
                ch = nextChar();
            }
            index--;
            id = Token.VARIABLE;
        } else {
            id = tokens.get(ch);
            if (id == Token.IMPL) nextChar();
        }
    }

    private char nextChar() {
        return index < str.length() ? str.charAt(index++) : Character.MIN_VALUE;
    }

    private Token id;
    private int index = 0;
    private StringBuilder variable;
    private String str = null;
}
