class Parser {
    static String subStrC(String s, int pos, int length){
        if (pos + length <= s.length())
            return s.substring(pos, pos + length);
            else return s.substring(pos, s.length());
    }

    static String parseImplication(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(')
                count++;
            if (s.charAt(i) == ')')
                count--;
            if (count == 0 && s.charAt(i) == '-') {
                return "(->," + parseDisjunction(subStrC(s,0, i)) + "," + parseImplication(subStrC(s,i + 2, s.length() - i - 2)) + ")";
            }
        }
        return parseDisjunction(s);
    }

    private static String parseDisjunction(String s) {
        int count = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '(')
                count++;
            if (s.charAt(i) == ')')
                count--;
            if (count == 0 && s.charAt(i) == '|') {
                return "(|," + parseDisjunction(subStrC(s, 0, i)) + "," + parseConjunction(subStrC(s, i + 1, s.length() - i - 1)) + ")";
            }
        }
        return parseConjunction(s);
    }

    private static String parseNegation(String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') count++;
            if (s.charAt(i) == ')') count--;
            if (count == 0 && s.charAt(i) == '!') {
                return "(!" + parseNegation(subStrC(s,i + 1, s.length()-i-1)) + ")";
            }
        }
        if (s.length() > 0 && s.charAt(0) == '(') {
            return Parser.parseImplication(subStrC(s, 1, s.length() - 2 ));
        } else return s;
    }


    private static String parseConjunction(String s) {
        int count = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '(')
                count++;
            if (s.charAt(i) == ')')
                count--;
            if (count == 0 && s.charAt(i) == '&') {
                return "(&," + parseConjunction(subStrC(s, 0, i)) + "," + parseNegation(subStrC(s,i + 1, s.length() - i)) + ")";
            }
        }
        return parseNegation(s);
    }
}
