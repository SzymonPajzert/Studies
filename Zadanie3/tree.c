#include <malloc.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "tree.h"

// ---------------------------- FACTORY FUNCTIONS ----------------------------

tree_ptr alloc_tree() {
    tree_ptr result  = (tree_ptr) malloc(sizeof(struct tree));
    result->parent_number = 0;
    return result;
}

tree_ptr create_variable(int var_num, tree_ptr value) {
    var_ptr variable = (var_ptr) malloc(sizeof(struct variable));
    variable->value = value;
    variable->var_num = var_num;
    tree_ptr result = alloc_tree();
    result->node.variable = variable;
    result->type = VARIABLE;
    return result;
}

tree_ptr create_number(int number) {
    num_ptr number_ptr = (num_ptr) malloc(sizeof(struct number));
    number_ptr->value = number;
    tree_ptr result = alloc_tree();
    result->node.number = number_ptr;
    result->type = NUMBER;
    return result;
}

tree_ptr create_operation(char operand, tree_ptr left, tree_ptr right) {
    op_ptr operation = (op_ptr) malloc(sizeof(struct operation));
    operation->left = left;
    operation->right = right;
    operation->operand = operand;
    tree_ptr result = alloc_tree();
    result->node.operation = operation;
    result->type = OPERATION;
    return result;
}

// ---------------------------- DESTRUCTORS ----------------------------

void remove_tree(tree_ptr tree) {
    switch(tree->type) {
        case OPERATION:
            remove_tree(tree->node.operation->left);
            remove_tree(tree->node.operation->right);
            free(tree->node.operation);
            break;
        case VARIABLE:
            remove_tree(tree->node.variable->value);
            free(tree->node.variable);
            break;
        case NUMBER:
            free(tree->node.number);
            break;
    }
    free(tree);
}

// ---------------------------- TRAVERSALS ----------------------------

/** Sets ids for elements of the tree.
 *
 * @param tree Tree to be labeled.
 * @param first_id First id to start from.
 * @return First non used value of id.
 */
int set_ids(tree_ptr tree, int first_id) {
    switch (tree->type) {
        case OPERATION:
            first_id = set_ids(tree->node.operation->left, first_id);
            first_id = set_ids(tree->node.operation->right, first_id);
            break;
        case VARIABLE:
            if(tree->node.variable->value) {
                first_id = set_ids(tree->node.variable->value, first_id);
            }
            break;
        case NUMBER: break;
    }
    tree->id = first_id++;
    return first_id;
}

/// Notify children about their parents existence
void notify_children(tree_ptr tree) {
    switch (tree->type) {
        case OPERATION:
            tree->node.operation->left->parent_number++;
            tree->node.operation->right->parent_number++;
            notify_children(tree->node.operation->right);
            notify_children(tree->node.operation->left);
            break;
        case VARIABLE:
            if(tree->node.variable->value) {
                tree->node.variable->value->parent_number++;
                notify_children(tree->node.variable->value);
            }
            break;
        case NUMBER: break;
    }
}

/// allocate parent to sizeof(int) * parent_num, set parent_num to 0;
void allocate_parent_space(tree_ptr tree) {
    tree->parents = malloc(sizeof(int) * tree->parent_number);
    tree->parent_number = 0;
    switch (tree->type) {
        case OPERATION:
            allocate_parent_space(tree->node.operation->right);
            allocate_parent_space(tree->node.operation->left);
            break;
        case VARIABLE:
            if(tree->node.variable->value) {
                allocate_parent_space(tree->node.variable->value);
            }
            break;
        case NUMBER: break;
    }
}

void set_parent(tree_ptr tree, int parent_id) {
    tree->parents[tree->parent_number++] = parent_id;
}

/// set your id to parent_num++ of child in parents
void insert_parents(tree_ptr tree) {
    switch (tree->type) {
        case OPERATION:
            set_parent(tree->node.operation->right, tree->id);
            set_parent(tree->node.operation->left, tree->id);
            break;
        case VARIABLE:
            if(tree->node.variable->value) {
                set_parent(tree->node.variable->value, tree->id);
            }
            break;
        case NUMBER: break;
    }
}

// ---------------------------- PARSING ----------------------------

void trim(char ** start, char ** end) {
    while(isspace(**start)) (*start)++;
    while(isspace(**end)) (*end)--;

    // delete parenthesis if they're the only ones
    if(strchr(*start, ')') == *end) {
        (*start)++;
        (*end)--;
    }

    while(isspace(**start)) (*start)++;
    while(isspace(**end)) (*end)--;

    **(++end) = '\0';
}

tree_ptr parse_right_hand(char * right_side, tree_ptr variables[], int V) {
    // trimming of the string
    char * start = right_side;
    char * end = strchr(right_side, '\0') - 1;
    trim(&start, &end);

    if(*start == '-') {
        tree_ptr minus_one = create_number(-1);
        tree_ptr result = create_operation('*', minus_one, parse_right_hand(start++, variables, V));
        return result;
    }

    switch (*start) {
        case '(': break;
        case '-': break;
    }

    int counter = 1;
    char * iter = start + 1;
    for(; iter != end && counter > 0; iter++) {
        if(*iter == '(') counter++;
        if(*iter == ')') counter--;
    }

    // We have operand expression with subtrees
    if(iter != end) {
        char operand = *(++iter);
        *(iter) = '\0';
        tree_ptr left_expr = parse_right_hand(start, variables, V);
        tree_ptr right_expr = parse_right_hand(++iter, variables, V);
    } else {

    }
}

tree_ptr parse_line(const char * line, tree_ptr variables[], int V) {
    char * eq_sign = strchr(line, '=');
    char * right_hand = eq_sign+1;
    *eq_sign = '\0';

    tree_ptr right_hand_expr = parse_right_hand(right_hand, variables, V);

    *strchr(line, ']') = '\0';
    int var_num = atoi(strchr(line, '['));
    return create_variable(var_num, right_hand_expr);
}

int transform(const char * line, tree_ptr variables[], int V) {
    tree_ptr expr = parse_line(line, variables, V);
    int var_num = expr->node.variable->var_num;
    if(variables[var_num]->node.variable->value) {
        return 0;
    } else {
        variables[var_num]->node.variable->value = expr;
        return 1;
    }
}

tree_ptr parse_tree(int lines, int V) {
    tree_ptr variables[V];
    for(int i=0; i<V; i++) {
        variables[i] = create_variable(i, NULL);
    }
    tree_ptr result = variables[0];

    for(int i=1; i<=lines; i++) {
        char * line = NULL;
        getline(&line, 0, stdin);
        if(transform(line, variables, V)) {
            printf("%d P\n", i);
        } else {
            printf("%d F\n", i);
            free(line);
            remove_tree(result);
            exit(1);
        }
        free(line);
    }

    set_ids(result, 0);
    notify_children(result);
    allocate_parent_space(result);
    insert_parents(result);
}

// ---------------------- printing ----------------------

const int indentation_step = 2;
void helper_print_tree(tree_ptr tree, int indentation) {
    // Indentation based on:
    // http://stackoverflow.com/questions/25609437/print-number-of-spaces-using-printf-in-c
    printf("%*s", indentation, "");
    printf("node_id: %d\n", tree->id);

    printf("%*s", indentation, "");
    printf("with parents: ");
    for(int i=0; i<tree->parent_number; i++) {
        printf("%d, ", tree->parents[i]);
    }
    printf("\n");

    switch (tree->type) {
        case OPERATION:
            printf("as operand: %c\n\n", tree->node.operation->operand);
            helper_print_tree(tree->node.operation->left, indentation+indentation_step);
            helper_print_tree(tree->node.operation->right, indentation+indentation_step);
            break;
        case VARIABLE:
            printf("as variable: x[%d]\n\n", tree->node.variable->var_num);
            if(tree->node.variable->value) {
                helper_print_tree(tree->node.variable->value, indentation+indentation_step);
            }
            break;
        case NUMBER:
            printf("as number: %ld\n\n", tree->node.number->value);
            break;
    }

}

void print_tree(tree_ptr tree) {
    helper_print_tree(tree, 0);
}