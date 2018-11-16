import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Solution {
    public static void main(String[] args) throws IOException {
        Parser p = new Parser();
        List<String> hyp = new ArrayList<>();
        List<String> forProof = new ArrayList<>();

        String s = new BufferedReader(new FileReader("input.txt")).readLine();
//        if (s.equals("B,W|=A->B")) {
//            System.out.println("B,W|-A->B\n" +
//                    "B->A->B\n" +
//                    "B\n" +
//                    "A->B");
//            return;
//        }
        s = Parser.deleteWhiteSpaces(s);

        int j = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ',') {
                hyp.add(p.parse(s.substring(j, i)).toString());
                j = i + 1;
            }
            if (s.charAt(i) == '|' && s.charAt(i + 1) == '=') {
                if (i != 0) {
                    hyp.add(p.parse(s.substring(j, i)).toString());
                }
                j = i + 2;
                break;
            }
        }

        StringBuilder ss = new StringBuilder();
        ss.append(s, j, s.length());
        s = ss.toString();
        forProof.add(s);

        for (int i = hyp.size() - 1; i >= 0; i--) {
            s = "(" + hyp.get(i) + ")->(" + s + ")";
            forProof.add(s);
        }
        if (forProof.size() > 1) {
            forProof.remove(forProof.size() - 1);
            Collections.reverse(forProof);
        }

        Solver solver = new Solver();
        solver.check(p.parse(s));
        solver.print(hyp, forProof);
    }
}