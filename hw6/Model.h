#include <iostream>
#include <string>
#include <set>
#include <map>
#include "Tree.h"
#include "Check.h"

#pragma once

using namespace std;

static set<string> validVars;

class Model {

private: static bool markImplication(string left, string right, int k) {
        isUsed[k] = true;

        for (int i = 0; i < worldsNumb; i++) {
            if (w[k][i] == 1 && !isUsed[i] && !markImplication(left, right, i))
                return false;
        }

        if (!Check().isValidInWorld(left, k) || Check().isValidInWorld(right, k)) {
            worlds[k].valid.emplace("(" + left + "->" + right + ")");
            //cout << "(" + left + "->" + right + ")"<<endl;
        } else
            return false;

        return true;
    }

private: void markOr(string left, string right, int k) {
        isUsed[k] = true;
        if (Check().isValidInWorld(left, k) || Check().isValidInWorld(right, k)) {
            worlds[k].valid.emplace("(" + left + "|" + right + ")");
            //cout << "(" + left + "|" + right + ")"<<endl;
        }

        for (int i = 0; i < worldsNumb; i++) {
            if (w[k][i] == 1 && !isUsed[i])
                markOr(left, right, i);
        }
    }

private: static void markAnd(string left, string right, int k) {
        isUsed[k] = true;
        if (Check().isValidInWorld(left, k) && Check().isValidInWorld(right, k)) {
            worlds[k].valid.emplace("(" + left + "&" + right + ")");
            //cout <<"(" + left + "&" + right + ")"<<endl;
        }
        for (int i = 0; i < worldsNumb; i++) {
            if (w[k][i] == 1 && !isUsed[i])
                markAnd(left, right, i);
        }
    }

private: bool markNo(string variable, int k) {
        isUsed[k] = true;
        if (Check().isValidInWorld(variable, k)) {
            return false;
        }

        for (int i = 0; i < worldsNumb; i++) {
            if (w[k][i] == 1 && !isUsed[i] && !markNo(variable, i)) {
                    return false;
            }
        }
        worlds[k].valid.emplace("!" + variable + "");
        //cout <<"!" + variable + ""<<endl;
        return true;
    }

public: string markModel(Tree expression) {
        string sb = "";

        if (expression.left != nullptr && expression.right != nullptr) {

            string strLeft = markModel(*expression.left);
            string strRight = markModel(*expression.right);
            Check().clearUsed();
            for (int i = 0; i < worldsNumb; i++) {
                if (!isUsed[i]) {
                    if (expression.str == "->") {
                        markImplication(strLeft, strRight, i);
                    } else if (expression.str == "&") {
                        markAnd(strLeft, strRight, i);
                    } else if ((expression.str == "|")) {
                        markOr(strLeft, strRight, i);
                    } else {
                        return "";
                    }
                }
            }
            sb += "(" + strLeft + expression.str + strRight + ")";

        } else if (expression.left != nullptr) {

            string strLeft = markModel(*expression.left);
            Check().clearUsed();
            for (int i = 0; i < worldsNumb; i++) {
                if (!isUsed[i])
                    markNo(strLeft, i);
            }
            sb += "!" + strLeft;
        } else {
            Check().clearUsed();
            for (int i = 0; i < worldsNumb; i++) {
                if (!isUsed[i]) {
                    Check().containVar(expression.str, i);
                }
                if (!fitKripke) {
                    return "";
                }
            }
            validVars.emplace(expression.str);
            return expression.str;
        }

        return sb;
    }

public: bool fitsKripke() {
        return fitKripke;
    }
};