#include <iostream>
#include <string>
#include <set>
#include <map>
#include "Tree.h"
#include <bits/stdc++.h>

using namespace std;

class Parser {
    private:string expression;
    private:int index = 0;

public: Parser(string expession) {
    index = 0;
    this->expression = expession;
}

    Tree* tokenParsing(int weight) {
        Tree* left = expressionParsing();
        while (true) {
            string operation = getToken();
            int newW = 0;
            try {
                newW = priorityMap.at(operation);
            } catch (std::out_of_range& e){
                newW = 0;
            }
            if ((newW == 1 && 2 <= weight) || (newW != 1 && newW <= weight)) {
                index -= operation.length();
                return left;
            }
            left = new Tree(operation, left, tokenParsing(newW));
        }
    }

private: string getToken() {
        if (index == expression.length()) {
            return "";
        }

        char c = expression[index];
        if (isalpha(c)) {
            string var = "";
            while (index < expression.length() && (isalpha(c) || isdigit(c))) {
                var += expression[index++];
                c = expression[index];
            }
            return var;
        }

        c = expression[index];
        if (c == '-') {
            index += 2;
            return "->";
        } else if (c == '(' || c == ')' || c == '&' || c== '|' || c == '!') {
            index += 1;
            return string(1, c);
        }
        return "";
    }

private: Tree* expressionParsing() {
        string currentToken = getToken();

        if (currentToken == "(") {
            Tree* result = tokenParsing(0);
            index++;
            return result;
        }

        if (isalpha(currentToken[0])) {
            return new Tree(currentToken);
        } else {
            return new Tree(currentToken,  expressionParsing());
        }
    }

    private: map<string, int> priorityMap = {{"->", 1}, {"|", 2}, {"&", 3}, {"!", 4}};

};
