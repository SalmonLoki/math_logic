import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class Solution {

    private static PParser parser = new PParser();
    private static Checker checker = new Checker();

    private static Vector<PParser.Tree> forest = new Vector<>();
    private static List<PParser.Tree> mpPrecalc = new ArrayList<>();
    private static HashMap<String, Integer> hypothesis = new HashMap<>();

    private static boolean isHypothesis(PParser.Tree tree) {
        return hypothesis.containsKey(tree.toString());
    }

    private static boolean isMP(PParser.Tree tree) {
        for (int i = forest.size() - 1; i >= 0; i--)
            if (forest.get(i).symb.equals("->")) {
                if (forest.get(i).right.equalWithoutSubst(tree))
                    for (int j = forest.size() - 1; j >= 0; j--)
                        if (forest.get(j).equalWithoutSubst(forest.get(i).left))
                            return true;
            }
        return false;
    }

    public static void main(String[] args) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("input.txt")));
        hypothesis = parser.parseHeader(in.readLine());

        boolean isCorrect = true;
        String str;

        for (int numbOfStr = 0; (str = in.readLine()) != null && isCorrect; numbOfStr++) {
            str = parser.clearGaps(str);
            if (str.length() == 0) {
                numbOfStr--;
            } else {
                PParser.Tree expression = parser.parse(str);
                forest.add(expression);

               /* if (numbOfStr + 1 == 503){
                    if (isHypothesis(expression)) System.out.println("hyp");
                    if (isMP(expression)) System.out.println("MP");
                    if (expression.isLogicAxiom()) System.out.println("isLAax");
                    if (expression.isFAAxiom()) System.out.println("isFA");

                }*/

                if      (!isHypothesis(expression) &&
                        !isMP(expression) &&
                        !expression.isLogicAxiom() &&
                        !expression.isFAAxiom() &&
                        !checker.IsAxiomPredicate(11, expression) &&
                        !checker.IsAxiomPredicate(12, expression) &&
                        !checker.isInductionScheme(expression) &&
                        isNotPredicateRule(1, expression) &&
                        isNotPredicateRule(2, expression))
                {
                    System.out.println("Вывод некорректен начиная с формулы номер : " + (numbOfStr + 1));
                    System.out.println(checker.mistake.toString());
                    isCorrect = false;
                }

                if (expression.Impl())
                    mpPrecalc.add(expression);
            }
        }
        if (isCorrect)
            System.out.println("Доказательство корректно.");
    }

    private static boolean wasEarlier(Integer rule, PParser.Tree phi, PParser.Tree quantPart) {
        String A = phi.toString();
        String B = quantPart.right.toString();
        if (rule == 2) {
            A = quantPart.right.toString();
            B = phi.toString();
        }

        for (PParser.Tree expression : forest.subList(0, forest.size() - 1))
            if (expression.Impl() && expression.left.toString().equals(A) && expression.right.toString().equals(B))
                return true;
        return false;
    }

    private static boolean varNotFree(PParser.Tree phi, String x) {
        if (phi.freeVariables.contains(x)) {
            checker.mistake.append("переменная ").append(x).append(" входит свободно в формулу ").append(phi.toString());
            return false;
        }
        return true;
    }

    // rule 1: (φ) → (ψ) => (φ) → ∀x(ψ)
    // rule 2: (ψ) → (φ) => ∃x(ψ) → (φ)
    private static boolean isNotPredicateRule(Integer rule, PParser.Tree expr) {
        PParser.Tree phi = expr.left;
        PParser.Tree quantPart = expr.right;
        if (rule == 2) {
            phi = expr.right;
            quantPart = expr.left;
        }
        return !(expr.fitFormPredictRule(rule, quantPart) && wasEarlier(rule, phi, quantPart) && varNotFree(phi, quantPart.left.toString()));
    }

}
