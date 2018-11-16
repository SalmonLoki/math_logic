#include <utility>
#include <iostream>
#include <string>
#include <set>
#pragma once

using namespace std;

class Tree {

public:
    string str;
    Tree *left;
    Tree *right;

public:
    explicit Tree(string expression) {
        str = expression;
        this->left = nullptr;
        this->right = nullptr;
    }

public:
    Tree(string expression, Tree *left) {
        str = expression;
        this->left = left;
        this->right = nullptr;
    }

public:
    Tree(string expression, Tree *left, Tree *right) {
        str = std::move(expression);
        this->left = left;
        this->right = right;
    }

};

class World {
    public:    int parent;
    public:    int indent;
    public:    set<string> valid;
};

