#ifndef TREE_H
#define TREE_H

typedef struct tree *tree_ptr;

typedef struct operation {
    char operand;
    tree_ptr left;
    tree_ptr right;
} *op_ptr;

typedef struct variable {
    int var_num;
    tree_ptr value;
} *var_ptr;

typedef struct number {
    long value;
} *num_ptr;

enum types {
    OPERATION, VARIABLE, NUMBER,
};

typedef struct tree {
    enum types type;
    int id;
    int* parents;
    int parent_number;
    union node_t {
        op_ptr operation;
        var_ptr variable;
        num_ptr number;
    } node;
} *tree_ptr;

tree_ptr parse_tree(int lines, int V);

void print_tree(tree_ptr tree);

void remove_tree(tree_ptr tree);

#endif //TREE_H
