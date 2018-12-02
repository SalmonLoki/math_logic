import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

class PParser {
    private Map<Character, Token> tokens = ((Supplier<Map<Character, Token>>) () -> {
        Map<Character, Token> t = new TreeMap<>();
        t.put('!', Token.NOT);
        t.put('|', Token.OR);
        t.put('&', Token.AND);
        t.put('-', Token.IMPL);
        t.put('(', Token.BRACE_LEFT);
        t.put(')', Token.BRACE_RIGHT);
        t.put('@', Token.QUANT_UNI);
        t.put('?', Token.QUANT_EXIST);
        t.put('=', Token.EQUAL);
        t.put('*', Token.MUL);
        t.put('+', Token.ADD);
        t.put('\'', Token.HATCH);
        t.put(Character.MIN_VALUE, Token.END);
        t.put('0', Token.ZERO);
        t.put(',', Token.COMM);
        return t;
    }).get();

    class Operation {
        final Token token;
        final String symb;
        final boolean lefty;

        Operation(Token token, String symb, boolean lefty) {
            this.token = token;
            this.symb = symb;
            this.lefty = lefty;
        }
    }
    private Operation[] operationsLogic = {
            new Operation(Token.IMPL, "->", true),
            new Operation(Token.OR, "|", false),
            new Operation(Token.AND, "&", false)
    };
    private Operation[] operationsFA = {
            new Operation(Token.ADD, "+", false),
            new Operation(Token.MUL, "*", false),
    };
    private Token id = null;
    private int index = 0;
    private StringBuilder string = new StringBuilder();
    private String str = null;

    private Map<Token, Supplier<Tree>> actionMult = ((Supplier<Map<Token, Supplier<Tree>>>) () -> {
        Map<Token, Supplier<Tree>> t = new EnumMap<>(Token.class);
        t.put(Token.FUNCTION, this::termsSequence);
        t.put(Token.SUBJ_VARIABLE, () -> new Tree(string.toString(), null, null, true));
        t.put(Token.BRACE_LEFT, this::term);
        t.put(Token.ZERO, () -> new Tree("0", null, null, false));
        return t;
    }).get();
    private Map<Token, Supplier<Tree>> actionUnary = ((Supplier<Map<Token, Supplier<Tree>>>) () -> {
        Map<Token, Supplier<Tree>> t = new EnumMap<>(Token.class);
        t.put(Token.NOT, () -> new Tree("!", unary(), null, false));
        t.put(Token.BRACE_LEFT, () -> {
            Tree tt;
            int i = index;
            char ch;
            int count = 1;
            while (count != 0) {
                ch = str.charAt(i);
                count += (ch == '(' ? 1 : 0);
                count += (ch == ')' ? -1 : 0);
                if (Character.toString(ch).matches("[-&|@?!=A-Z]")) {
                    tt = expressionLogic();
                    parseToken();
                    return tt;
                }
                i++;
            }
            tt = predicate();
            return tt;
        });
        t.put(Token.QUANT_UNI, () -> quantAction("@"));
        t.put(Token.QUANT_EXIST, () -> quantAction("?"));
        t.put(Token.PREDICATE_VAR, () -> {
            Tree tt = new Tree(string.toString(), null, null, false);
            parseToken();
            return tt;
        });
        return t;
    }).get();

    HashMap<String, Integer> parseHeader(String firstLine) {
        HashMap<String, Integer> hypothesis = new HashMap<>();
        firstLine = clearGaps(firstLine).split("\\|-")[0];
        int count = 0;
        int balance = 0;
        int last = -1;
        for (int i = 0; i < firstLine.length(); i++) {
            switch (firstLine.charAt(i)) {
                case '(':
                    balance++;
                    break;
                case ')':
                    balance--;
                    break;
                case ',':
                    if (balance == 0) {
                        hypothesis.put(parse(firstLine.substring(last + 1, i)).toString(), count++);
                        last = i;
                    }
            }
        }
        if (firstLine.length() != 0)
            hypothesis.put(parse(firstLine.substring(last + 1)).toString(), count+1);
        return hypothesis;
    }

    private Tree expressionLogic() {
        return expression(0, operationsLogic, this::unary);
    }

    private Tree term() {
        return expression(0, operationsFA, this::multiplied);
    }

    private Tree expression(int level, Operation[] operations, Supplier<Tree> simpleTree) {
        if (level >= operations.length) return simpleTree.get();

        Tree tree = expression(level + 1, operations, simpleTree);
        while (id == operations[level].token) {
            tree = new Tree(
                    operations[level].symb,
                    tree,
                    operations[level].lefty ? expression(level, operations, simpleTree) : expression(level + 1, operations, simpleTree),
                    false
            );
        }
        return tree;
    }

    private Tree multiplied() {
        parseToken();
        Tree tree = actionMult.get(id).get();
        parseToken();
        while (id == Token.HATCH) {
            tree = new Tree("'", tree, null, false);
            parseToken();
        }
        return tree;
    }

    private Tree unary() {
        parseToken();
        Supplier<Tree> tree = actionUnary.get(id);
        return tree != null ? tree.get() : predicate();
    }

    private Tree variable() {
        parseToken();
        return new Tree(string.toString(), null, null, false);
    }

    private Tree quantAction(String symb) {
        Tree tt = new Tree(symb, variable(), unary(), false);
        tt.freeVariables.add(tt.left.symb);
        tt.removeIfQuant();
        return tt;
    }

    private void readTerm(Tree tt) {
        Tree term = term();
        tt.terms.add(term);
        tt.freeVariables.addAll(term.freeVariables);
        tt.removeIfQuant();
    }

    private Tree termsSequence() {
        Tree tt = new Tree(string.toString(), null, null, false);
        readTerm(tt);
        while (id == Token.COMM) readTerm(tt);
        return tt;
    }

    private Tree predicate() {
        Tree tt;
        if (id == Token.PREDICATE) {
            tt = termsSequence();
            parseToken();
        } else {
            index -= ((id == Token.FUNCTION || id == Token.SUBJ_VARIABLE) ? string.length() : 1);
            tt = new Tree("=", term(), term(), false);
        }
        return tt;
    }

    String clearGaps(String s) {
        return Arrays.stream(s.split("\\s")).reduce(String::concat).orElse(null);
    }

    Tree parse(String s) {
        str = clearGaps(s);
        index = 0;
        return expressionLogic();
    }

    private void parseToken() {
        string = new StringBuilder();
        boolean bigLetter = false;

        char ch = nextChar();
        if (Character.isAlphabetic(ch)) {
            if (ch >= 'A' && ch <= 'Z') bigLetter = true;
            string.append(ch);
            ch = nextChar();
            while (Character.isDigit(ch)) {//имя переменной содержит цифры
                string.append(ch);
                ch = nextChar();
            }
            if (bigLetter) {  //большая буква
                if (ch == '(') //предикат вида A123(
                    id = Token.PREDICATE;
                else { //предикат вида A123
                    index--;
                    id = Token.PREDICATE_VAR;
                }
            } else { //маленькая буква
                if (id == Token.QUANT_UNI || id == Token.QUANT_EXIST || ch != '(') {
                    id = Token.SUBJ_VARIABLE; //вида a123
                    index--;
                } else { //вида a123(
                    id = Token.FUNCTION;
                }
            }
        } else {
            id = tokens.get(ch); //знак операции или 0
            if (ch == '-') nextChar();
        }
    }

    private char nextChar() {
        return index < str.length() ? str.charAt(index++) : Character.MIN_VALUE;
    }


    private enum Token {
        IMPL, OR, AND, NOT, QUANT_UNI, QUANT_EXIST, PREDICATE, FUNCTION, HATCH, PREDICATE_VAR,
        ADD, MUL, ZERO, COMM, EQUAL, BRACE_LEFT, BRACE_RIGHT, SUBJ_VARIABLE, END
    }

    static class Tree {
        static Checker checker = new Checker();
        String symb;
        Tree left;
        Tree right;
        ArrayList<Tree> terms = new ArrayList<>();
        Set<String> freeVariables = new HashSet<>();

        Tree(String symb, Tree left, Tree right, boolean addSign) {
            this.symb = symb;
            this.left = left;
            this.right = right;
            if (left != null) this.freeVariables.addAll(left.freeVariables);
            if (right != null) this.freeVariables.addAll(right.freeVariables);
            if (addSign) this.freeVariables.add(this.symb);
            removeIfQuant();
        }

        void removeIfQuant() {
            if (this.QuantUni() || this.QuantExist())
                this.freeVariables.remove(this.left.symb);
        }


        boolean Impl() { return this.symb.equals("->"); }

        boolean Conj() { return this.symb.equals("&"); }

        boolean QuantUni() { return this.symb.equals("@"); }

        boolean QuantExist() { return this.symb.equals("?"); }

        boolean BinaryOperation() { return Arrays.asList("->", "|", "&", "=", "+", "*").contains(this.symb); }

        boolean fitFormInduction() {
            return this.Impl() && this.left.Conj() && this.left.right.QuantUni() && this.left.right.right.Impl();
        }

        boolean fitFormAxiom(Integer axiom, Tree quantPart) {
            return this.Impl() && ((axiom == 11 && quantPart.QuantUni())) || (axiom == 12 && quantPart.QuantExist());
        }

        boolean fitFormPredictRule(Integer rule, Tree quantPart) {
            return this.Impl() && ((rule == 1 && quantPart.QuantUni()) || (rule == 2 && quantPart.QuantExist()));
        }

        boolean isLogicAxiom() {
            for (Tree axiom : checker.axiomsLogic) {
                if (Checker.isLA(this, axiom, new HashMap<>()))
                    return true;
            }
            return false;
        }

        boolean isFAAxiom() {
            for (Tree axiom : checker.axiomsFA) {
                if (axiom.equalWithoutSubst(this)) {
                    return true;
                }
            }
            return false;
        }



        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            if (BinaryOperation()) {
                result
                        .append("(")
                        .append(left.toString())
                        .append(symb)
                        .append(right.toString())
                        .append(")");
            } else if (symb.equals("!")) {
                result
                        .append(symb)
                        .append(left.toString());
            } else if (symb.equals("\'")) {
                result
                        .append(left.toString())
                        .append("\'");
            } else if (symb.equals("?") || symb.equals("@")) {
                result
                        .append(symb)
                        .append(left.toString())
                        .append("(")
                        .append(right.toString())
                        .append(")");
            } else if (!terms.isEmpty()) {
                StringBuilder builderTerms = new StringBuilder(symb + "(");
                boolean first = true;
                for (Tree tree : terms) {
                    if (first) first = false;
                    else builderTerms.append(",");
                    builderTerms.append(tree.toString());
                }
                result.append(builderTerms).append(")");
            } else
                result.append(symb);

            return result.toString();
        }


        boolean equalWithoutSubst(Tree tree2) {
            if (tree2 == null || !this.symb.equals(tree2.symb) || this.terms.size() != tree2.terms.size())
                return false;

            for (int i = 0; i < this.terms.size(); i++)
                if (!this.terms.get(i).toString().equals(tree2.terms.get(i).toString()))
                    return false;

            boolean flage;
            if (left == null && tree2.left == null) {
                flage = true;
            } else {
                flage = (left != null) && left.equalWithoutSubst(tree2.left);
            }
            if (right == null && tree2.right == null) {
                return flage;
            } else {
                return flage && (right != null) && right.equalWithoutSubst(tree2.right);
            }
        }

        boolean equalWithSubst(Tree tree2) {
            String t = this.symb;
            if (tree2 == null) return false;

            if (this.BinaryOperation() || this.QuantUni() || this.QuantExist())
                return t.equals(tree2.symb) && this.left.equalWithSubst(tree2.left) && this.right.equalWithSubst(tree2.right);

            if (t.equals("!") || t.equals("\'"))
                return t.equals(tree2.symb) && this.left.equalWithSubst(tree2.left);

            if (!this.terms.isEmpty()) {
                return t.equals(tree2.symb) &&
                        this.terms.size() == tree2.terms.size() &&
                        IntStream.range(0, this.terms.size())
                                .allMatch(i -> this.terms.get(i).equalWithSubst(tree2.terms.get(i)));
            }
            Checker.vars.putIfAbsent(t, tree2);
            return Checker.vars.get(t).equalWithoutSubst(tree2);
        }


        Tree variableChange(String var, Tree theta) {
            if (this.terms.isEmpty() && this.symb.equals(var)) {
                return theta;
            }
            if (this.left == null && this.right == null)
                return new Tree(this.symb, null, null, false);
            else if (this.left == null)
                return new Tree(this.symb, null, this.right.variableChange(var, theta), false);
            else if (this.right == null)
                return new Tree(this.symb, this.left.variableChange(var, theta),  null, false);
            else
                return new Tree(this.symb, this.left.variableChange(var, theta), this.right.variableChange(var, theta), false);
        }
    }
}