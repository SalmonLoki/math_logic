import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Solver {
    private static Parser parser = new Parser();

    private static List<Parser.Tree> axioms = Stream.of("A -> B -> A", "(A -> B) -> (A -> B -> C) -> (A -> C)", "A -> B -> A & B", "A & B -> A",
            "A & B -> B", "A -> A | B", "B -> A | B", "(A -> C) -> (B -> C) -> (A | B -> C)",
            "(A -> B) -> (A -> !B) -> !A", "!!A -> A"
    ).map(parser::parse).collect(Collectors.toList());

    Vector<String> result = new Vector<>();
    private Map<String, Integer> mpPrecalc = new HashMap<>();
    private Map<String, Integer> revForest = new HashMap<>();
    private Vector<Parser.Tree> trees = new Vector<>();

    private HashSet<String> supp = new HashSet<>();

    private static boolean isAxiom(Parser.Tree node, Parser.Tree axiom, HashMap<String, String> variables) {

        if (axiom == null) {
            return node == null;
        } else if (axiom.sign.matches("\\w+")) {
            if (variables.containsKey(axiom.sign)) {
                return variables.get(axiom.sign).equals(node.toString());
            } else {
                variables.put(axiom.sign, node.toString());
                return true;
            }
        } else if (axiom.sign.equals(node.sign)) {
            return isAxiom(node.left, axiom.left, variables) &&
                    isAxiom(node.right, axiom.right, variables);
        } else {
            return false;
        }

    }

    private static boolean isAxiom(Parser.Tree node) {
        for (Parser.Tree axiom : axioms) {
            if (isAxiom(node, axiom, new HashMap<>())) {
                return true;
            }
        }
        return false;
    }

    String makeFirstStr(String firstStr) {
        String[] ss = firstStr.split(",|\\|-");
        StringBuilder firstNew = new StringBuilder();
        for (int i = 0; i < ss.length - 2; i++) {
            supp.add(parser.parse(ss[i]).toString());
            if (i != 0) {
                firstNew.append(",");
            }
            firstNew.append(ss[i]);
        }
        firstNew.append("|-");//.append(ss[ss.length - 2]).append(")->(").append(ss[ss.length - 1]).append(")");
        firstNew.append(new Parser.Tree("->", parser.parse(ss[ss.length - 2]), parser.parse(ss[ss.length - 1])).toString());
        result.add(firstNew.toString());
        return ss[ss.length - 2];
    }

    void buildMP(String A, String B, String C) {
        result.add(parser.parse("((" + A + ")->(" + B + "))->(((" + A + ")->(" + B + ")->(" + C + "))->((" + A + ")->(" + C + ")))").toString());
        result.add(parser.parse("((" + A + ")->(" + B + ")->(" + C + "))->((" + A + ")->(" + C + "))").toString());
        result.add(parser.parse("((" + A + ")->(" + C + "))").toString());
    }

    void buildAinA(String A) {
        String aa = parser.parse("((" + A + ")->(" + A + "))").toString();
        result.add(parser.parse("((" + A + ")->" + aa + ")").toString());
        result.add(parser.parse("((" + A + ")->" + aa + ")->((" + A + ")->(" + aa + "->(" + A + ")))->" + aa ).toString());
        result.add(parser.parse("((" + A + ")->(" + aa + "->(" + A + ")))->" + aa ).toString());
        result.add(parser.parse("((" + A + ")->(" + aa + "->(" + A + ")))").toString());
        result.add(aa);
    }

    private boolean isMP(Parser.Tree tree, String last_supp, List<String> expressions, String current_str) {
        String s = tree.toString();
        if (mpPrecalc.containsKey(s)) {
            int first = mpPrecalc.get(s);
            String s2 = trees.get(first).left.toString();
            if (revForest.containsKey(s2)) {
                int second = revForest.get(s2);
                buildMP(last_supp, trees.get(second).toString(), current_str);
                return true;
            }
        }
        return false;
    }

    void solve(List<String> expressions) {

        String firstStr = makeFirstStr(expressions.get(0));
        Parser.Tree first = parser.parse(firstStr);

        for (int i = 0; i < expressions.size() - 1; ++i) {
            Parser.Tree tree = parser.parse(expressions.get(i + 1));
            expressions.set(i + 1, tree.toString());
            trees.add(tree);
            String s = tree.toString();
            Parser.Tree newTree = new Parser.Tree("->", first, tree);
            if (first.equals(tree)) {
                buildAinA(first.toString());
            } else if (supp.contains(s) || isAxiom(tree)) {
                result.add(parser.parse("((" + s + ")->((" + firstStr + ")->(" + s + "))").toString());
                result.add(s);
                result.add(parser.parse("((" + firstStr + ")->(" + s + "))").toString());
            } else {
                isMP(tree, firstStr, expressions, s);
            }

            if (tree.sign.equals("->")) {
                mpPrecalc.put(tree.right.toString(), i);
            }
            revForest.putIfAbsent(tree.toString(), i);
        }
    }

}
