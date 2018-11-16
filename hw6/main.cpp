#include <iostream>
#include <fstream>
#include <vector>
#include <regex>
#include "Parser.h"
#include <math.h>
#include <bitset>
#include <algorithm>
#include "Model.h"
#include "Check.h"
#include "Alg.h"

using namespace std;


void ReplaceString(string& subject, const string& search, const std::string& replace) {
    size_t pos = 0;
    while ((pos = subject.find(search, pos)) != std::string::npos) {
        subject.replace(pos, search.length(), replace);
        pos += replace.length();
    }
}


int main() {
    freopen("input.txt", "r", stdin);
    string s;
    vector<string> lines;
    while (getline(cin, s)) {
        if (s.length() > 0)
            lines.push_back(s);
    }

    worldsNumb = lines.size() - 1;
    for (int i = 0; i < worldsNumb; i++) {
        w.emplace_back();
        for (int j = 0; j < worldsNumb; j++) {
            w[i].push_back(-1);
        }
    }

    for (int i = 0; i < worldsNumb; i++) {
        isUsed.push_back(false);
    }

    int newWorldNumber = 0;

    for (int i = 1; i <= worldsNumb; i++) {
        World newWorld;
        string str = lines[i];
        regex reg("\\*");
        sregex_token_iterator it{str.begin(), str.end(), reg, -1};
        vector<string> strIndentVars{it, {}};
        newWorld.indent = strIndentVars[0].length();


        if (strIndentVars.size() == 1)////////////
        {
            strIndentVars.push_back("");
            //strIndentVars[1] = "";
        }
        string strVars = strIndentVars[1];
        ReplaceString(strVars, " ", "");


        if (strVars.length() > 0) {
            regex comm (",");
            sregex_token_iterator it2{strVars.begin(), strVars.end(), comm, -1};
            vector<string> separatedVars{it2, {}};

            set<string> tempSet;
            for (string tempStr: separatedVars){
                tempSet.insert(tempStr);
            }
            newWorld.valid = tempSet;
/*
                for (string s:tempSet){
                    cout<<s<<endl;
                }
                cout <<endl;*/

        }


        if (newWorld.indent != 0) {
            if (newWorld.indent == worlds[newWorldNumber - 1].indent + 1) {
                newWorld.parent = newWorldNumber - 1;

            } else {
                int nwParent = worlds[newWorldNumber - 1].parent;
                while (newWorld.indent <= worlds[nwParent].indent) {
                    nwParent = worlds[nwParent].parent;
                }
                newWorld.parent = nwParent;

            }
            w[newWorld.parent][newWorldNumber] = 1;
        }
        newWorldNumber++;
        worlds.push_back(newWorld);
    }


    string expression = Model().markModel(*Parser(lines[0]).tokenParsing(0));
   //cout << expression <<endl;

    if (!Model().fitsKripke()) {
        cout << "Не модель Крипке" << endl;
        return 0;
    }

  /*  for (World world:worlds){
        for (string s:world.valid){
            cout<<s<<endl;
        }
        cout <<endl;
    }*/

    //cout<< worlds.size() <<endl;
    if (!Check().disprovesExpr(expression)) {
        cout << "Не опровергает формулу" << endl;
        return 0;
    }

    Alg *g = new Alg();
    g->printAlgG();
    g->printVariableValues();

    return 0;
}







