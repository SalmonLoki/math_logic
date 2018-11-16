import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Solution {

    private static Parser parser = new Parser();

    private static List<Parser.Tree> axioms = Stream.of("A -> B -> A", "(A -> B) -> (A -> B -> C) -> (A -> C)", "A -> B -> A & B", "A & B -> A",
            "A & B -> B", "A -> A | B", "B -> A | B", "(A -> C) -> (B -> C) -> (A | B -> C)",
            "(A -> B) -> (A -> !B) -> !A", "!!A -> A"
            ).map(parser::parse).collect(Collectors.toList());


    private static Vector<Parser.Tree> forest = new Vector<>();
    private static Map<String, Integer> revForest = new HashMap<>();
    private static Map<String, Integer> mpPrecalc = new HashMap<>();
    private static HashMap<String, Integer> hypothesis = new HashMap<>();


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

    private static boolean isAxiom(Parser.Tree node, int counter) {
        for (int i = 0; i < axioms.size(); i++) {
            if (isAxiom(node, axioms.get(i), new HashMap<>())) {
                System.out.printf("(%d) %s (Сх. акс. %d)\n", counter + 1, node.toString(), i + 1);
                return true;
            }
        }
        return false;
    }
    /*
    private static boolean isMP(Parser.Tree tree, int counter) {
        for (int i = counter - 1; i >= 0; --i) {
            if (forest.get(i).sign.equals("->") && forest.get(i).right.equals(tree)) {
                Parser.Tree leftTree = forest.get(i).left;
                for (int z = counter - 1; z >= 0; --z) {
                    if (forest.get(z).equals(leftTree)) {
                        System.out.printf("(%d) %s (M.P. %d, %d)\n", counter + 1, tree, i + 1, z + 1);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    */
    private static boolean isMP(Parser.Tree tree, int counter) {
        String s = tree.toString();
        if (mpPrecalc.containsKey(s)) {
            int first = mpPrecalc.get(s);
            String s2 =forest.get(first).left.toString();
            if (revForest.containsKey(s2)) {
                int second = revForest.get(s2);
                System.out.printf("(%d) %s (M.P. %d, %d)\n", counter + 1, tree, first + 1, second + 1);
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s = Parser.deleteWhiteSpaces(in.readLine());
        int count = 0;
        String[] hypos = s.split("(,|\\|-)");
        for (String s1 : Arrays.asList(hypos).subList(0, Math.max(hypos.length - 1, 0))) {
            if (s1.length() > 0)
                hypothesis.put(parser.parse(s1).toString(), ++count);
        }


        for (int counter = 0; (s = in.readLine()) != null; ++counter) {
            s = Parser.deleteWhiteSpaces(s);
            if (s.length() == 0) {
                --counter;
                continue;
            }
            Parser.Tree tree = parser.parse(s);
            forest.add(tree);
            if (hypothesis.containsKey(tree.toString())) {
                System.out.printf("(%d) %s (Предп. %d)\n", counter + 1, s, hypothesis.get(tree.toString()));
            } else if (!isAxiom(tree, counter) && !isMP(tree, counter)) {
                System.out.printf("(%d) %s (Не доказано)\n", counter + 1, tree);
            }

            if (tree.sign.equals("->")) {
                mpPrecalc.put(tree.right.toString(), counter);
            }
            revForest.putIfAbsent(tree.toString(), counter);

        }
    }
}
