import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

public class Solution {
    Vector<Vector<Integer>> graph = new Vector<>();
    Vector<Vector<Integer>> reverseGraph = new Vector<>();
    boolean[] usedA;
    boolean[] usedB;
    boolean[] usedTog;

    int n;

    Vector<Vector<Integer>> sum = new Vector<>();
    Vector<Vector<Integer>> mul = new Vector<>();
    Vector<Vector<Integer>> impl = new Vector<>();


    int k;
    void dfs(int v, boolean rev, boolean[] used, boolean decrease) {
        if (decrease)
            if (usedTog[v])
                k--;
        used[v] = true;
        for (int to : rev ? reverseGraph.get(v) : graph.get(v)) {
            if (! used[to])
                dfs(to, rev, used, decrease);
        }
    }


    boolean sumMul(boolean direction) {
        for (int a = 0; a < n; ++a) {
            for (int b = a + 1; b < n; ++b) {
                usedA = new boolean[n];
                dfs(a, direction, usedA, false);
                usedB = new boolean[n];
                dfs(b, direction, usedB, false);

                usedTog = new boolean[n];
                int currK = 0;
                for (int i = 0; i < n; ++i) {
                    usedTog[i] = usedA[i] && usedB[i];
                    if (usedTog[i]) currK++;
                }
                boolean f = true;
                for (int c = 0; c < n; ++c) {
                    if (usedTog[c]) {
                        k = currK;
                        usedB = new boolean[n];
                        dfs(c, direction, usedB, true);
                        if (k == 0) {
                            if (direction) {
                                mul.get(a).set(b, c);
                                mul.get(b).set(a, c);
                            } else {
                                sum.get(a).set(b, c);
                                sum.get(b).set(a, c);
                            }
                            f = false;
                            break;
                        }
                    }
                }

                if (f) {
                    if (!direction)
                        System.out.printf("%s%d%s%d\n", "Операция '+' не определена: ", a + 1, "+", b + 1);
                    else
                        System.out.printf("%s%d%s%d\n", "Операция '*' не определена: ", a + 1, "*", b + 1);
                    return true;
                }
            }
        }
        return false;
    }


    boolean isDistribut() {
        for (int a = 0; a < n; ++a) {
            for (int b = 0; b < n; ++b) {
                for (int c = 0; c < n; ++c) {
                    if (!  mul.get(a).get(sum.get(b).get(c)).equals(sum.get(mul.get(a).get(b)).get(mul.get(a).get(c)))  ) {
                        System.out.printf("%s%d%s%d%s%d%s\n", "Нарушается дистрибутивность: ", a + 1, "*(", b + 1, "+", c + 1, ")");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean isImplic() {
        for (int a = 0; a < n; ++a) {
            for (int b = 0; b < n; ++b) {
                usedTog = new boolean[n];
                int currK = 0;
                k = 0;
                for (int c = 0; c < n; ++c) {
                    int ml = mul.get(a).get(c);
                    usedB = new boolean[n];
                    dfs(ml, false, usedB, true);
                    usedTog[c] = usedB[b];
                    if (usedTog[c]) currK++;
                }

                boolean f = true;
                for (int c = 0; c < n; ++c) {
                    if (usedTog[c]) {
                        k = currK;
                        usedB = new boolean[n];
                        dfs(c, true, usedB, true);
                        if (k == 0) {
                            impl.get(a).set(b, c);
                            f = false;
                            break;
                        }
                    }
                }
                if (f) {
                    System.out.printf("%s%d%s%d\n", "Операция '->' не определена: ", a + 1, "->", b + 1);
                    return true;
                }

            }
        }
        return false;
    }


    boolean isBul() {
        int one = impl.get(0).get(0);
        int zero = 0;
        for (int i = 0; i < n; ++i) {
            if (reverseGraph.get(i).isEmpty()) {
                zero = i;
                break;
            }
        }
        for (int a = 0; a < n; ++a) {
            if (sum.get(a).get(impl.get(a).get(zero)) != one) {
                System.out.printf("%s%d%s%d\n", "Не булева алгебра: ", a + 1, "+~", a + 1);
                return true;
            }
        }
        return false;
    }



    Solution() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        n = Integer.parseInt(in.readLine());
        for (int i = 0; i < n; i++) {
            graph.add(new Vector<>());
            reverseGraph.add(new Vector<>());
        }
        for (int i = 0; i < n; i++) {
            StringTokenizer st = new StringTokenizer(in.readLine());
            while (st.hasMoreTokens()) {
                int t = Integer.parseInt(st.nextToken()) - 1;
                if (t != i) {
                    graph.get(i).add(t);
                    reverseGraph.get(t).add(i);
                }
            }
        }


        for (int i = 0; i < n; i++) {
            Vector<Integer> t = new Vector<>();
            t.setSize(n);
            t.set(i, i);
            sum.add(t);
        }
        for (int i = 0; i < n; i++) {
            Vector<Integer> t = new Vector<>();
            t.setSize(n);
            t.set(i, i);
            mul.add(t);
        }
        for (int i = 0; i < n; i++) {
            Vector<Integer> t = new Vector<>();
            t.setSize(n);
            impl.add(t);
        }
        if (sumMul(false)
                || sumMul(true)
                || isDistribut()
                || isImplic()
                || isBul()) return;

        System.out.println("Булева алгебра\n");

    }

    public static void main(String[] args) throws Exception {
        new Solution();
    }
}
