import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static class Tree {
        int num = 0;
        String value = "";
        Tree parent = null;
        Tree left = null;
        Tree right = null;
        Map<String, Boolean> boolVars = new HashMap<>();
    }

    private static Map<String, Boolean> variables = new HashMap<>();

    private static Tree termTree;


    private static void makeTree(String s, Tree world) {
        boolean flag = false;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '&' || s.charAt(i) == '|' || s.charAt(i) == '-') {
                world.value = "" + s.charAt(i);
                int j;
                if (s.charAt(i + 1) == '>') j = i + 2;
                else
                    j = i + 1;
                int count = 0;
                for (int k = j + 1; k < s.length(); k++) {
                    if (s.charAt(k) == '(') count++;
                    if (s.charAt(k) == ')') count--;
                    if (s.charAt(k) == ',' && count == 0) {
                        world.left = new Tree();
                        world.right = new Tree();
                        makeTree(Parser.subStrC(s,j + 1, k - j - 1), world.left);
                        makeTree(Parser.subStrC(s, k + 1, s.length() - k - 2), world.right);
                        flag = true;
                        break;
                    }
                }
                if (flag) break;
            } else {
                if (s.charAt(i) == '!') {
                    world.value = "" + s.charAt(i);
                    world.left = new Tree();
                    makeTree(s.substring(i + 1, s.length() - 1), world.left);
                    break;
                }
                int j = i;
                if (world.value.equals("")) {
                    while (j < s.length() && s.charAt(j) != '(' && s.charAt(j) != ')') {
                        world.value += s.charAt(j);
                        j++;
                    }
                    if (j != i) {
                        variables.put(world.value, false);
                    }
                }
            }
        }
    }

    private static void setWorld(Tree world) {
        world.left = new Tree();
        world.right = new Tree();
        world.left.parent = world;
        world.right.parent = world;
        world.boolVars = new HashMap<>(variables);
        world.left.boolVars = new HashMap<>();
        world.right.boolVars = new HashMap<>();
    }

    private static void makeWorld(String s, Tree world) {
        String tmp = Parser.subStrC(s, 1, s.length() - 2);
        if (tmp.charAt(0) == 'x' && tmp.charAt(tmp.length() - 1) == 'x') {
            setWorld(world);
        } else if (tmp.charAt(0) == 'x') {
            setWorld(world);
            tmp = Parser.subStrC(tmp, 1, tmp.length() - 1);
            makeWorld(tmp, world.right);
        } else if (tmp.charAt(tmp.length() - 1) == 'x') {
            setWorld(world);
            tmp = Parser.subStrC(tmp,0, tmp.length() - 1);
            makeWorld(tmp, world.left);
        } else {
            int pos = 0;
            for (int i = 0; i < tmp.length() - 1; i++) {
                if (tmp.charAt(i) == ')' && tmp.charAt(i + 1) == '(') {
                    pos = i;
                    break;
                }
            }
            setWorld(world);
            makeWorld(Parser.subStrC(tmp,0, pos + 1), world.left);
            makeWorld(Parser.subStrC(tmp,pos + 1, tmp.length() - pos + 1), world.right);
        }
    }

    private static int number = 0;

    private static void justNumbers(Tree world) {
        world.num = number;
        number++;
        if (world.left != null) {
            justNumbers(world.left);
        } else {
            return;
        }
        if (world.right != null)
            justNumbers(world.right);
    }

    private static int[] values = new int[9];
    private static boolean flag = false;

    private static void appBoolVars(Tree world) {
        world.boolVars = world.num == 0 ? new HashMap<>(variables) : new HashMap<>(world.parent.boolVars);
        int ch = 1;
        for (Map.Entry<String, Boolean> j : world.boolVars.entrySet()) {
            if (values[world.num] == ch) {
                j.setValue(true);
            }
            ch++;
        }
        if (world.left != null) {
            appBoolVars(world.left);
            if (world.right != null) {
                appBoolVars(world.right);
            }
        }
    }

    private static void app(int pos) {
        if (!flag) {
            values[pos]++;
            if (values[pos] == variables.size() + 1) {
                if (pos == 0) {
                    values[pos] = 0;
                    flag = true;
                    return;
                }
                values[pos] = 0;
                app(pos - 1);
            }
        }
    }

    private static void appDisjunction(String left, String right, Tree world) {
        world.boolVars.put("(|," + left + "," + right + ")", world.boolVars.get(left) || world.boolVars.get(right));
        if (world.left != null) {
            appDisjunction(left, right, world.left);
        }
        if (world.right != null) {
            appDisjunction(left, right, world.right);
        }
    }

    private static void appConjuction(String left, String right, Tree world) {
        world.boolVars.put("(&," + left + "," + right + ")", world.boolVars.get(left) && world.boolVars.get(right));
        if (world.left != null) {
            appConjuction(left, right, world.left);
        }
        if (world.right != null) {
            appConjuction(left, right, world.right);
        }
    }


    private static boolean appImplication(String left, String right, Tree world) {
        boolean value = true;
        if (world.left != null) {
            value &= appImplication(left, right, world.left);
        }
        if (world.right != null) {
            value &= appImplication(left, right, world.right);
        }
        if (value && (!world.boolVars.get(left) || world.boolVars.get(right))) {
            world.boolVars.put("(->," + left + "," + right + ")",  true);
            return true;
        } else {
            world.boolVars.put("(->," + left + "," + right + ")",  false);
            return false;
        }
    }

    private static boolean appNegation(String left, Tree world) {
        boolean value = true;
        if (world.left != null) {
            value &= appNegation(left, world.left);
        }
        if (world.right != null) {
            value &= appNegation(left, world.right);
        }

        if (value && !world.boolVars.get(left)) {
            world.boolVars.put("(!" + left + ")", true);
            return true;
        } else {
            world.boolVars.put("(!" + left + ")", false);
            return false;
        }
    }

    private static String apply(Tree world, Tree modelTree) {
        if (world == null) {
            return "";
        }
        switch (world.value) {
            case "|": {
                String left = apply(world.left, modelTree);
                String right = apply(world.right, modelTree);
                appDisjunction(left, right, modelTree);
                return "(|," + left + "," + right + ")";
            }
            case "&": {
                String left = apply(world.left, modelTree);
                String right = apply(world.right, modelTree);
                appConjuction(left, right, modelTree);
                return "(&," + left + "," + right + ")";
            }
            case "-": {
                String left = apply(world.left, modelTree);
                String right = apply(world.right, modelTree);
                appImplication(left, right, modelTree);
                return "(->," + left + "," + right + ")";
            }
            case "!": {
                String left = apply(world.left, modelTree);
                appNegation(left, modelTree);
                return "(!" + left + ")";
            }
            default:
                return world.value;
        }
    }

    private static boolean checkFullExpression(Tree world, String fullExpression) {

        return (world.left == null || checkFullExpression(world.left, fullExpression)) &&
                (world.right == null || checkFullExpression(world.right, fullExpression)) &&
                world.boolVars.get(fullExpression);
    }

    private static boolean checkExpressionTree(Tree world) {
        String term = apply(termTree, world);
        return checkFullExpression(world, term);
    }

    private static boolean setVariables(Tree world) {
        for (int i = 0; i < values.length; i++) {
            values[i] = 0;
        }
        while (!flag) {
            appBoolVars(world);
            if (!checkExpressionTree(world)) {
                return false;
            }
            app(8);
        }
        return true;
    }

    private static String[] models = new String[]{
            "((((xx)x)x)x)",
            "(((xx)(xx))x)",
            "((x((xx)x))x)",
            "((x(x(xx)))x)",
            "(((xx)x)(xx))",
            "((x(xx))(xx))",
            "((xx)((xx)x))",
            "((xx)(x(xx)))",
            "(x(((xx)x)x))",
            "(x((x(xx))x))",
            "(x((xx)(xx)))",
            "(x(x((xx)x)))",
            "(x(x(x(xx))))",
            "(((x(xx))x)x)"
    };

    private static Tree[] trees = new Tree[14];

    private static Set<Tree> base = new HashSet<>();

    private static Set<Integer> tmp = new TreeSet<>();

    private static Vector<Set<Integer>> finalBase = new Vector<>();

    private static void makeBase(Tree world) {
        base.add(world);
        if (world.left != null) {
            makeBase(world.left);
        }
        if (world.right != null) {
            makeBase(world.right);
        }
    }

    private static Set<Integer> pushNum(Tree world) {
        tmp.add(world.num);
        if (world.left != null) {
            pushNum(world.left);
        }
        if (world.right != null) {
            pushNum(world.right);
        }
        return new TreeSet<>(tmp);
    }

    private static void smallTree(Set<Tree> base) {
        for (Tree tree : base) {
            tmp = new TreeSet<>();
            finalBase.add(pushNum(tree));
        }
    }

    private static Set<Set<Integer>> topology = new LinkedHashSet<>();

    private static void makeTopology(List<Set<Integer>> top) {
        Set<Integer> newTop;
        for (int i = 0; i < (1 << top.size()); i++) {
            newTop = new TreeSet<>();
            int help = i;
            for (int j = top.size() - 1; j >= 0; j--) {
                if (help % 2 == 1) {
                    newTop.addAll(top.get(j));
                }
                help /= 2;
            }
            topology.add(newTop);
        }
    }

    private static List<Set<Integer>> algGey =
            Stream.generate(TreeSet<Integer>::new).limit(47).collect(Collectors.toList());

    private static boolean compare(Set<Integer> set1, Set<Integer> set2) {
        return set2.containsAll(set1);
    }

    private static void makeAlgebra(Set<Set<Integer>> mySet) {
        int ch = 0;
        for (Set<Integer> i : mySet) {
            int chh = 0;
            for (Set<Integer> j : mySet) {
                if (compare(i, j)) {
                    algGey.get(ch).add(chh);
                }
                chh++;
            }
            ch++;
        }
    }

    private static Set<Integer> unusedWorlds = new TreeSet<>();

    private static void findWorld(String var, Tree world) {
        if (world.boolVars.get(var)) {
            unusedWorlds.add(world.num);
        }
        if (world.left != null) {
            findWorld(var, world.left);
        }
        if (world.right != null) {
            findWorld(var, world.right);
        }
    }

    private static int makeCorrectWorld(Set<Set<Integer>> t) {
        int ch = 0;
        for (Set<Integer> j : t) {
            if (j.equals(unusedWorlds)) {
                return ch + 1;
            }
            ch++;
        }
        return Integer.MIN_VALUE;
    }

    private static int isGeneral() {
        int treeNumber = -1;
        for (int i = 0; i < 14; i++) {
            trees[i] = new Tree();
            makeWorld(models[i], trees[i]);
            number = 0;
            justNumbers(trees[i]);
            if (!setVariables(trees[i])) {
                treeNumber = i;
                break;
            }
        }
        return treeNumber;
    }

    public static void main(String... args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s = in.readLine();
        while (s.length() == 0) s = in.readLine();
        String ss = Parser.parseImplication(s);
        termTree = new Tree();
        makeTree(ss, termTree);
        int t = isGeneral();
        if (t == -1) {
            System.out.println("Формула общезначима");
            return;
        }
        makeBase(trees[t]);
        smallTree(base);
        makeTopology(finalBase);
        makeAlgebra(topology);
        System.out.println(topology.size());
        for (int i = 0; i < topology.size(); i++) {
            Set<Integer> anAlgGey = algGey.get(i);
            for (int j : anAlgGey) {
                System.out.print(j + 1 + " ");
            }
            System.out.println();
        }
        boolean f = true;
        for (String i : variables.keySet()) {
            unusedWorlds.clear();
            findWorld(i, trees[t]);
            if (f) {
                f = false;
                System.out.print(i + "=" + makeCorrectWorld(topology));
            } else {
                System.out.print("," + i + "=" + makeCorrectWorld(topology));
            }

        }
        System.out.println();
    }
}