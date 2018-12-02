import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Checker {
    private static PParser parserCh = new PParser();
    static Map<String, PParser.Tree> vars = new HashMap<>();

    StringBuilder mistake = new StringBuilder();
    List<PParser.Tree> axiomsLogic = Stream.of(
            "A -> B -> A",
            "(A -> B) -> (A -> B -> C) -> (A -> C)",
            "A -> B -> A & B",
            "A & B -> A",
            "A & B -> B",
            "A -> A | B",
            "B -> A | B",
            "(A -> C) -> (B -> C) -> (A | B -> C)",
            "(A -> B) -> (A -> !B) -> !A",
            "!!A -> A"
    ).map(parserCh::parse).collect(Collectors.toList());
    List<PParser.Tree> axiomsFA = Stream.of(
            "a = b -> a' = b'",
            "a = b -> a = c -> b = c",
            "a' = b' -> a = b",
            "!a' = 0",
            "a + b' = (a + b)'",
            "a + 0 = a",
            "a * 0 = 0",
            "a * b' = a * b + a"
    ).map(parserCh::parse).collect(Collectors.toList());

    static boolean isLA(PParser.Tree tree, PParser.Tree axiom, HashMap<String, String> variables) {
        if (axiom == null) return tree == null;

        else if (axiom.symb.matches("\\w+")) {
            String var = axiom.symb;
            if (variables.containsKey(var)) {
                return variables.get(var).equals(tree.toString());
            } else {
                variables.put(var, tree.toString());
                return true;
            }
        } else
            return axiom.symb.equals(tree.symb) && isLA(tree.left, axiom.left, variables) && isLA(tree.right, axiom.right, variables);
    }

    // ψ[x := 0] & @x((ψ)->(ψ)[x := x']) -> ψ
    // a & @x(b->c) -> d
    boolean isInductionScheme(PParser.Tree expr) {
        if (expr.fitFormInduction()) {
            PParser.Tree a = expr.left.left; //a = ψ[x := 0]
            PParser.Tree b = expr.left.right.right.left;//b = (ψ)
            PParser.Tree c = expr.left.right.right.right; //c = (ψ)[x := x']
            PParser.Tree d = expr.right;  //d = ψ
            String x = expr.left.right.left.symb;
            return  b.equalWithoutSubst(d) &&
                    a.equalWithoutSubst(b.variableChange(x, new PParser.Tree("0", null, null, false))) &&
                    c.equalWithoutSubst(b.variableChange(x, new PParser.Tree("\'", new PParser.Tree(x, null, null, true), null, false)));
        }
        return false;
    }
    //&& expr.left.right.left.equalWithoutSubst(new PParser.Tree(x, null, null, true));

    // axiom 11: ∀x(ψ)->(ψ[x := θ])     @xb->substitPart
    // axiom 12: (ψ[x := θ]) → ∃x(ψ)     substitPart->?xb
    // θ свободна для подстановки вместо х (после подстановки θ вместо своб.вхожд. x ни одно своб.вхожд.переменной в θ не станет связанным)
    boolean IsAxiomPredicate(Integer axiom, PParser.Tree expr) {
        PParser.Tree quantPart = expr.left;
        PParser.Tree substitPart = expr.right;
        if (axiom == 12) {
            quantPart = expr.right;
            substitPart = expr.left;
        }
        if (expr.fitFormAxiom(axiom, quantPart)) {
            PParser.Tree b = quantPart.right;
            String x = quantPart.left.symb;

            vars = new HashMap<>();
            if (b.equalWithSubst(substitPart)) { //change vars vars(x, θ)

                PParser.Tree theta = vars.get(x);
                if (theta!=null) {
                    Set<String> set = substitPart.freeVariables;
                    if (theta.freeVariables.stream().anyMatch(v -> !set.contains(v))) {
                        mistake.append("терм ").append(theta.toString())
                                .append(" не свободен для подстановки в формулу ")
                                .append(b.toString()).append(" вместо переменной ").append(x);
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}