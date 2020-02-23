#ifndef TREE_H
#define TREE_H

typedef struct tree *tree_ptr;

typedef struct operation {
    char operand;
    int left_first_parent;
    tree_ptr left;
    int right_first_parent;
    tree_ptr right;
} *op_ptr;

typedef struct variable {
    int var_num;
    int first_parent;
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

/// Saves pointer to each node in array.
/// Assumes size of array is big enough.
void get_vertices(tree_ptr tree, tree_ptr * array);

/// Saves id to each variable in array
/// Assumes size of array is big enough.
void get_variables(tree_ptr tree, int * array);

#endif //TREE_H
