#include <iostream>
#include <fstream>
#include <vector>
#include <regex>
#include "Tree.h"
#include <math.h>
#include <bitset>
#include <algorithm>

#pragma once

using namespace std;

static bool fitKripke = true;
static vector<World> worlds;
static vector<bool> isUsed;
static long worldsNumb = -1;
static vector<vector<int>> w;

class Check {

public:   static bool check(string formula, int k, bool changeBool) {
        isUsed[k] = true;

        for (int i = 0; i < worldsNumb; i++) {
            if (w[k][i] == 1 && !isUsed[i] && !check(formula, i, false)) {
                if (changeBool)
                    fitKripke = false;
                return false;
            }
        }

        return (isValidInWorld(formula, k));
    }


    static void clearUsed() {
        for (int i = 0; i < worldsNumb; i++) {
            isUsed[i] = false;
        }
    }


    static void containVar(string varible, int k) {
        isUsed[k] = true;

        if (isValidInWorld(varible, k)) {
            check(varible, k, true);
            if (!fitKripke) {
                return;
            }
        }

        for (int i = 0; i < worldsNumb; i++) {
            if (w[k][i] == 1 && !isUsed[i]) {
                containVar(varible, i);
            }
        }
    }

    static bool isValidInWorld(string formula, int k) {
        return (worlds[k].valid.count(formula) != 0);
    }

    static bool disprovesExpr(const string &expression){


        Check().clearUsed();
        bool b = false;
        for (int i = 0; i < worldsNumb; i++) {
            if (!isUsed[i] && ! Check().check(expression, i, false)) {
                b = true;
                break;
            }
        }
        return b;
    }

};
