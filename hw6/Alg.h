#include <iostream>
#include <fstream>
#include <vector>
#include <regex>
#include <math.h>
#include <bitset>
#include <algorithm>
#include <set>
#include "Check.h"
#include "Model.h"

#pragma once
using namespace std;

class Alg{
private:
    set<int> base;
    set<int> topology;
    vector<int> ordG;
    vector<vector<int>> al;

public:
    Alg(){
        Check().clearUsed();
        base.emplace(0);
        for (int i = 0; i < worldsNumb; i++) {
            if (!isUsed[i]) {
                createBase(i);
            }
        }

        topology = createTopology(base);

        copy(topology.begin(), topology.end(), back_inserter(ordG));
        sort(ordG.begin(), ordG.end());

        al = createAlg(ordG);
    }

    string toBinaryString(int i) {
        string bits = bitset<sizeof(i) * 8>(static_cast<unsigned long long int>(i)).to_string();
        bits = bits.substr(min(bits.length() - 1, bits.find('1')));
        return bits;
    }

    int createBase(int k) {
        isUsed[k] = true;

        int num = 1 << k;
        for (int i = 0; i < worldsNumb; i++) {
            if (w[k][i] == 1 && !isUsed[i]) {
                num = createBase(i) | num;
            }
        }
        base.emplace(num);
        return num;
    }

    set<int> createTopology(set<int> b){
        set<int> top;
        vector<int> ordBase;

        copy(b.begin(), b.end(), back_inserter(ordBase));
        sort(ordBase.begin(), ordBase.end());

        for (int i = 0; i < pow(2, ordBase.size()); i++) {
            string bits = toBinaryString(i);
            int t = 0;
            for (int j = 0; j < bits.length(); j++) {
                if (bits[j] == '1') {
                    t = t | ordBase[j];
                }
            }
            top.emplace(t);
        }
        return top;
    }

    vector<vector<int>> createAlg(vector<int> ordG){
        vector<vector<int>> a;
        for (int i = 0; i < ordG.size(); i++) {
            a.emplace_back();
        }

        for (int i = 0; i < ordG.size(); i++) {
            for (int j = i; j < ordG.size(); j++) {
                if ((ordG[i] | ordG[j]) == ordG[j]) {
                    a[i].push_back(j);
                }
            }
        }
        return a;
    }

    void printAlgG(){
        int count = 0;
        for (int i = 0; i < ordG.size(); i++) {
            if (al[i].size()> 0) count++;
        }
        cout << count << endl;
        for (int i = 0; i < ordG.size(); i++) {
            for (int j = 0; j < al[i].size(); j++) {
                cout << (al[i][j] + 1) << " ";
            }
            if (al[i].size() != 0)
                cout << endl;
        }
    }

    void printVariableValues(){
        vector<string> vars;
        copy(validVars.begin(), validVars.end(), back_inserter(vars));

        for (int j = 0; j < vars.size(); j++) {
            int numb = 0;
            Check().clearUsed();
            for (int i = 0; i < worldsNumb; i++) {
                if (!isUsed[i]) {
                    numb |= getNumByVarible(vars[j], i);
                }
            }
            if (j != 0) {
                cout << ",";
            }
            cout << vars[j] << "=";

            for (int i = 0; i < ordG.size(); i++) {
                if (ordG[i] == numb) {
                    cout << (i + 1);
                    break;
                }
            }
        }
    }

    static int getNumByVarible(string var, int k) {
        isUsed[k] = true;
        int num = 0;

        if (worlds[k].valid.count(var) != 0) {
            num = 1 << k;
        } else {
            return 0;
        }
        for (int i = 0; i < worldsNumb; i++) {
            if (w[k][i] == 1 && !isUsed[i]) {
                num = getNumByVarible(var, i) | num;
            }
        }
        return num;
    }


};