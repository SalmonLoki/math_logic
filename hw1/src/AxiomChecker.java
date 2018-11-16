public class AxiomChecker {
    private static final String[] regexes = new String[]{
            "\\((.*)->\\((.*)->\\1\\)\\)",
            "\\(\\((.*)->(.*)\\)->\\(\\(\\1->\\(\\2->(.*)\\)\\)->\\(\\1->\\3\\)\\)\\)",
            "\\((.*)->\\((.*)->\\(\\1&\\2\\)\\)\\)",
            "\\(\\((.*)&(.*)\\)->\\1\\)",
            "\\(\\((.*)&(.*)\\)->\\2\\)",
            "\\((.*)->\\(\\1\\|(.*)\\)\\)",
            "\\((.*)->\\((.*)\\|\\1\\)\\)",
            "\\(\\((.*)->(.*)\\)->\\(\\((.*)->\\2\\)->\\(\\(\\1\\|\\3\\)->\\2\\)\\)\\)",
            "\\(\\((.*)->(.*)\\)->\\(\\(\\1->!\\2\\)->!\\1\\)\\)",
            "\\(!!(.*)->\\1\\)"
    };
    private static final String[] raws = new String[]{
            "A -> B -> A", "(A -> B) -> (A -> B -> C) -> (A -> C)", "A -> B -> A & B", "A & B -> A",
            "A & B -> B", "A -> A | B", "B -> A | B",
            "(A -> C) -> (B -> C) -> (A | B -> C)",
            "(A -> B) -> (A -> !B) -> !A", "!!A -> A"
    };
    final static Parser parser = new Parser();

    public static void main(String[] args) {
        for (int i = 0; i < raws.length; i++) {
            System.out.println(parser.parse(raws[i]).toString().matches(regexes[i]));
        }
    }
}
