import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class Solution {
    public static void main(String[] args) throws FileNotFoundException {
        Solver a = new Solver();
        List<String> expressions =
        new BufferedReader(new FileReader(new File("input.txt")))
                .lines().map(Parser::deleteWhiteSpaces).filter(s -> s.length()>0).collect(Collectors.toList());
        a.solve(expressions);
        a.result.forEach(System.out::println);
    }
}